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
        deviceList = bluetoothProvider.getDeviceList()
    }

    fun connectDevice(device: Device) {
        bluetoothProvider.connect(device)
    }
}
