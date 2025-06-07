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
    override fun getDeviceList(): List<Device> {
        throw UnsupportedOperationException("getDeviceList is not supported on iOS")
    }
    override fun connect(device: Device) {
        throw UnsupportedOperationException("connect is not supported on iOS")
    }
    override fun disconnect() {
        throw UnsupportedOperationException("disconnect is not supported on iOS")
    }
}

actual fun getBluetoothProvider(): BluetoothProvider = IOSBluetoothProvider()
