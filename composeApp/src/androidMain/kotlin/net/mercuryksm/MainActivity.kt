package net.mercuryksm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val bluetoothProvider = getBluetoothProvider()
        val viewModel = BluetoothViewModel(bluetoothProvider)

        // TODO: replace this, only for provisional purposes
        ContextHolder.initialize(applicationContext)
        println("ContextHolder initialized with context: ${ContextHolder.context}")

        setContent {
            App(
                viewModel = viewModel
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        viewModel = BluetoothViewModel(getBluetoothProvider())
    )
}
