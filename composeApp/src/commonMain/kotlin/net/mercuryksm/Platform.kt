package net.mercuryksm

import net.mercuryksm.device.Device

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface BluetoothProvider {
    fun isBluetoothAvailable(): Boolean
    fun getDeviceList(): List<Device>
interface BluetoothProvider {
    fun isBluetoothAvailable(): Boolean
    fun getDeviceList(): List<Device>

    /**
     * Attempts to connect to the specified device.
     * @param device The device to connect to
     * @return true if connection initiated successfully, false otherwise
     * @throws IllegalArgumentException if device is invalid
     * @throws SecurityException if permissions are insufficient
     */
    fun connect(device: Device): Boolean

    /**
     * Disconnects from the currently connected device.
     * @return true if disconnection initiated successfully, false otherwise
     */
    fun disconnect(): Boolean
}
}

expect fun getBluetoothProvider(): BluetoothProvider
