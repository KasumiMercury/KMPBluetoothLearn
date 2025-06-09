package net.mercuryksm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.mercuryksm.device.Device

class BluetoothViewModel(
    private val bluetoothProvider: BluetoothProvider
) {
    var showBluetoothButton by mutableStateOf(bluetoothProvider.isBluetoothAvailable())
        private set

    var deviceList by mutableStateOf<List<Device>>(emptyList())
        private set

    fun loadDeviceList() {
        bluetoothProvider.getDeviceList { devices ->
            deviceList = devices
        }
    }

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, FAILED
    }

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.DISCONNECTED)
        private set

    fun connectDevice(device: Device) {
        if (connectionState == ConnectionState.CONNECTING) return

        connectionState = ConnectionState.CONNECTING
        try {
            bluetoothProvider.connect(device)
            connectionState = ConnectionState.CONNECTED
        } catch (e: Exception) {
            connectionState = ConnectionState.FAILED
            throw e
        }
    }
}
