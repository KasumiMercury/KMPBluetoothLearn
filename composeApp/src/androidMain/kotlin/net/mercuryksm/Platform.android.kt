package net.mercuryksm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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

    override fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    override fun getDeviceList(): List<Device> {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                throw UnsupportedOperationException("Bluetooth is not enabled or not available.")
            }

            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
            if (pairedDevices.isEmpty()) {
                return emptyList()
            }

            val deviceList = mutableListOf<Device>()

            pairedDevices.filter {
                it.uuids?.any { uuid -> uuid.toString() == "2c081c6d-61dd-4af8-ac2f-17f2ea5e5214" } == true
            }.forEach { device ->
                val deviceName = device.name ?: "Unknown Device"

                deviceCache[device.address] = device

                deviceList.add(
                    Device(
                        name = deviceName,
                        address = device.address
                    )
                )
            }

            return deviceList
        } else {
            throw SecurityException("Bluetooth permission is not granted.")
        }
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
                        val deviceName = if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            bluetoothDevice.name
                        } else {
                            "Unknown Device"
                        }
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            println("Connected to $deviceName")
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            println("Disconnected from $deviceName")
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        val deviceName = if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            bluetoothDevice.name
                        } else {
                            "Unknown Device"
                        }
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            println("Services discovered for $deviceName")
                        } else {
                            println("Failed to discover services for $deviceName, status: $status")
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
