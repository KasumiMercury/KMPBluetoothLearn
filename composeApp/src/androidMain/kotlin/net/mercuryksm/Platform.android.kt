package net.mercuryksm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
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

    override fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    override fun getDeviceList(): List<Device> {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                throw UnsupportedOperationException("Bluetooth is not enabled or not available.")
            }

            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
            if (pairedDevices.isEmpty()) {
                return emptyList()
            }

            val deviceList = pairedDevices.filter {
                it.uuids?.any { uuid -> uuid.toString() == "2c081c6d-61dd-4af8-ac2f-17f2ea5e5214" } == true
            }.map { device ->
                Device(
                    name = device.name ?: "Unknown Device",
                    address = device.address
                )
            }

            return deviceList
        } else {
            throw SecurityException("Bluetooth permission is not granted.")
        }
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
