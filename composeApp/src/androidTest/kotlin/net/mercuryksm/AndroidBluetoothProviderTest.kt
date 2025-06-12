package net.mercuryksm

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidBluetoothProviderTest {

    private lateinit var mockAdapter: BluetoothAdapter
    private lateinit var mockContext: Context
    private lateinit var provider: AndroidBluetoothProvider

    @Before
    fun setUp() {
        mockAdapter = mockk()
        mockContext = mockk<Context>(relaxed = true)

        every { mockContext.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.applicationContext } returns mockContext

        provider = AndroidBluetoothProvider(mockContext, mockAdapter)
    }

    @Test
    fun getDeviceList_throwsException_whenBluetoothDisabled() {
        every { mockAdapter.isEnabled } returns false

        try {
            provider.getDeviceList { }
            fail("UnsupportedOperationException がスローされませんでした")
        } catch (e: UnsupportedOperationException) {
            assertTrue(e.message?.contains("Bluetooth is not enabled or not available") == true)
        }
    }
}
