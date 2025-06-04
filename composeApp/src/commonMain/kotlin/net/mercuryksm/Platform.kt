package net.mercuryksm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface BluetoothProvider {
    fun isBluetoothAvailable(): Boolean
    fun getDeviceName(): String
}

expect fun getBluetoothProvider(): BluetoothProvider
