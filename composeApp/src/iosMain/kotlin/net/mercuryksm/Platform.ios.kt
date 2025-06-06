package net.mercuryksm

import net.mercuryksm.device.Device
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class IOSBluetoothProvider : BluetoothProvider {
    // this project does not support Bluetooth on iOS
    override fun isBluetoothAvailable(): Boolean = false
    override fun getDeviceNameList(): List<Device> {
        throw UnsupportedOperationException("Getting device name list is not supported on iOS")
    }
}

actual fun getBluetoothProvider(): BluetoothProvider = IOSBluetoothProvider()
