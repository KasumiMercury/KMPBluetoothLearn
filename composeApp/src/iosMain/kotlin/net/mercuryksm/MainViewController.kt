package net.mercuryksm

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val bluetoothProvider = getBluetoothProvider()
    val viewModel = BluetoothViewModel(bluetoothProvider)
    App(
        viewModel = viewModel
    )
}
