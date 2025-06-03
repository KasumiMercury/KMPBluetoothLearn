package net.mercuryksm

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class IOSBluetoothProvider : BluetoothProvider {
    // this project does not support Bluetooth on iOS
    override fun isBluetoothAvailable(): Boolean = false
}

actual fun getBluetoothProvider(): BluetoothProvider = IOSBluetoothProvider()
