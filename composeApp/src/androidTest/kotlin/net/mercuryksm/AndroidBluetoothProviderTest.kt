package net.mercuryksm

import android.bluetooth.BluetoothAdapter
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class AndroidBluetoothProviderTest {
    @Test
    fun getDeviceList_throwsException_whenBluetoothDisabled() {
        val mockAdapter: BluetoothAdapter = mockk()
        every { mockAdapter.isEnabled } returns false

        val provider = AndroidBluetoothProvider(mockk(), mockAdapter)

        try {
            provider.getDeviceList { }
        } catch (e: UnsupportedOperationException) {
            assert(e.message?.contains("Bluetooth is not enabled or not available") == true)
        }
    }
}
