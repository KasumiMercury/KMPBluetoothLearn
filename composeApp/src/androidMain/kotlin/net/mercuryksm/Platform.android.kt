package net.mercuryksm

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

class AndroidBluetoothProvider : BluetoothProvider

actual fun getBluetoothProvider(): BluetoothProvider = AndroidBluetoothProvider()
