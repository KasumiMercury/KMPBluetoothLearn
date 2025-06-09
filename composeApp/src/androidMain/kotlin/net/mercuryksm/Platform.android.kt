package net.mercuryksm

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
import android.os.Build
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

    override fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    override fun getDeviceList(callback: (List<Device>) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Bluetooth permission is not granted.")
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            throw UnsupportedOperationException("Bluetooth is not enabled or not available.")
        }

        val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            callback(emptyList())
            return
        }

        val foundDevices = mutableMapOf<String, BluetoothDevice>()
        val filter = ScanFilter.Builder()
            .setServiceUuid(android.os.ParcelUuid(serviceUuid))
            .build()
        val filters = listOf(filter)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (!foundDevices.containsKey(device.address)) {
                    foundDevices[device.address] = device
                    deviceCache[device.address] = device
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                for (result in results) {
                    val device = result.device
                    if (!foundDevices.containsKey(device.address)) {
                        foundDevices[device.address] = device
                        deviceCache[device.address] = device
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                callback(emptyList())
            }
        }

        scanner.startScan(filters, settings, scanCallback)

        android.os.Handler(context.mainLooper).postDelayed({
            scanner.stopScan(scanCallback)
            val deviceList = foundDevices.values.map { device ->
                Device(
                    name = device.name ?: "Unknown Device",
                    address = device.address
                )
            }
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

            if (bluetoothDevice.bondState != BluetoothDevice.BOND_BONDED) {
                throw IllegalStateException("Device is not bonded: ${bluetoothDevice.name}")
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
