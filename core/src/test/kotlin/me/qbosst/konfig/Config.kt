package me.qbosst.konfig

import me.qbosst.konfig.engine.JsonEngine

class Config: Konfig() {
    val dbName: String by requiredItem("name")
    val dbHost: String by requiredItem("host")
    val dbPassword: String by requiredItem("password")

    val inner by requiredObject(::InnerConfig)

    class InnerConfig(parent: Konfigurable) : KonfigObject(parent) {
        var int: Int by requiredItem()
    }
}

fun main() {
    val config = Config()
    config.init("config.json", JsonEngine())

    println(config.dbName)
    println(config.dbHost)
    println(config.dbPassword)

    println(config.inner.int)
    config.inner.int = 5
    println(config.inner.int)
    config.inner.int = 0
}