package net.mercuryksm

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID
import net.mercuryksm.device.Device

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

class AndroidBluetoothProvider(
    private val context: Context
) : BluetoothProvider {
    private val bluetoothManager: BluetoothManager? =
        ContextCompat.getSystemService(context, BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val deviceCache = mutableMapOf<String, BluetoothDevice>()

    private val serviceUuid = UUID.fromString("2c081c6d-61dd-4af8-ac2f-17f2ea5e5214")

    private var activeScanner: BluetoothLeScanner? = null
    private var activeScanCallback: ScanCallback? = null

    override fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    private fun stopActiveScan() {
        activeScanCallback?.let { callback ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            activeScanner?.stopScan(callback)
        }
        activeScanner = null
        activeScanCallback = null
    }

    override fun getDeviceList(callback: (List<Device>) -> Unit) {
        val tag = "AndroidBluetoothProvider"

        stopActiveScan()

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "BLUETOOTH_SCAN permission is not granted.")
            callback(emptyList())
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(tag, "BLUETOOTH_CONNECT permission is not granted. Device names may not be available.")
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            throw UnsupportedOperationException("Bluetooth is not enabled or not available.")
        }

        val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(tag, "BluetoothLeScanner is not available.")
            callback(emptyList())
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = mutableListOf<ScanFilter>()
        val serviceFilter = ScanFilter.Builder()
            .setServiceUuid(android.os.ParcelUuid(serviceUuid))
            .build()
        filters.add(serviceFilter)

        val foundDevices = mutableMapOf<String, BluetoothDevice>()


        val scanCallback = object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                Log.d(
                    tag,
                    "onScanResult: Found device -> Name: ${device.name ?: "N/A"}, Address: ${device.address}, RSSI: ${result.rssi}"
                )
                if (!foundDevices.containsKey(device.address)) {
                    foundDevices[device.address] = device
                    deviceCache[device.address] = device
                }
            }

            @SuppressLint("MissingPermission")
            override fun onBatchScanResults(results: List<ScanResult>) {
                Log.d(tag, "onBatchScanResults: ${results.size} results")
                for (result in results) {
                    val device = result.device
                    if (!foundDevices.containsKey(device.address)) {
                        Log.d(
                            tag,
                            "onBatchScanResults: Found device -> Name: ${device.name ?: "N/A"}, Address: ${device.address}"
                        )
                        foundDevices[device.address] = device
                        deviceCache[device.address] = device
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                val errorMessage = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Application registration failed"
                    SCAN_FAILED_INTERNAL_ERROR -> "Internal error"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "Feature unsupported"
                    else -> "Unknown error"
                }
                Log.e(tag, "Scan failed with error code: $errorCode - $errorMessage")
                callback(emptyList())
            }
        }

        activeScanner = scanner
        activeScanCallback = scanCallback

        Log.d(tag, "Starting Bluetooth scan...")
        scanner.startScan(
            filters,
            settings,
            scanCallback
        )

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(tag, "Stopping Bluetooth scan after 3 seconds...")
            scanner.stopScan(scanCallback)
            val deviceList = foundDevices.values.map { device ->
                Device(
                    name = device.name ?: "Unknown Device",
                    address = device.address
                )
            }
            Log.d(tag, "Scan completed. Found ${deviceList.size} devices.")
            callback(deviceList)
        }, 3000)
    }

    override fun connect(device: Device) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothDevice = deviceCache[device.address]
                ?: throw IllegalArgumentException("Device not found in cache: ${device.address}")

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                throw UnsupportedOperationException("Bluetooth is not enabled or not available.")
            }

            bluetoothDevice.connectGatt(
                context,
                false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            throw SecurityException("Bluetooth connect permission is not granted.")
                        }
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            println("Connected to ${bluetoothDevice.name}")
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            println("Disconnected from ${bluetoothDevice.name}")
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            throw SecurityException("Bluetooth connect permission is not granted.")
                        }
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            println("Services discovered for ${bluetoothDevice.name}")
                        } else {
                            println("Failed to discover services for ${bluetoothDevice.name}, status: $status")
                        }
                    }
                }
            )
        } else {
            throw SecurityException("Bluetooth connect permission is not granted.")
        }
    }

    override fun disconnect() {
        TODO("Implement disconnect logic")
    }
}

// TODO: fix this
@SuppressLint("StaticFieldLeak")
object ContextHolder {
    private lateinit var _context: Context
    val context: Context
        get() = _context

    val isInitialized: Boolean
        get() = ::_context.isInitialized

    fun initialize(context: Context) {
        _context = context.applicationContext
    }
}

actual fun getBluetoothProvider(): BluetoothProvider {
    // TODO: fix
    println("getBluetoothProvider called")
    if (!ContextHolder.isInitialized) {
        throw IllegalStateException("ContextHolder is not initialized. Call ContextHolder.initialize(context) first.")
    }
    return AndroidBluetoothProvider(ContextHolder.context)
}
