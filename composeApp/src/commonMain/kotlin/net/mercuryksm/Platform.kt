package net.mercuryksm

import net.mercuryksm.device.Device

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface BluetoothProvider {
    fun isBluetoothAvailable(): Boolean
    fun getDeviceList(): List<Device>
    fun connect(device: Device)
    fun disconnect()
}

expect fun getBluetoothProvider(): BluetoothProvider
