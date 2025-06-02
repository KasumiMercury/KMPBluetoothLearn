package net.mercuryksm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface BluetoothProvider {}

expect fun getBluetoothProvider(): BluetoothProvider
