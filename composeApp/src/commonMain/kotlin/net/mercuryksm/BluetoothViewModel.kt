package net.mercuryksm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BluetoothViewModel(
    private val bluetoothProvider: BluetoothProvider
) {
    var showBluetoothButton by mutableStateOf(bluetoothProvider.isBluetoothAvailable())
        private set
}
