package me.qbosst.konfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.qbosst.konfig.engine.SerializationEngine
import java.io.File

open class Konfig: Konfigurable() {
    override lateinit var engine: SerializationEngine<*, *>
    final override val parent: Konfigurable? get() = null

    lateinit var path: String
    private val file: File get() = File(path)

    fun init(path: String, engine: SerializationEngine<*, *>, failIfNotExists: Boolean = true) {
        this.path = path
        this.engine = engine

        if(!file.exists()) {
            file.createNewFile()

            items.values.forEach { item ->
                item.encodeDefault(engine)
            }

            write()

            if(failIfNotExists) {
                throw KonfigException("A config file has been generated at ${file.absolutePath}. Please fill it in.")
            }
        } else {
            fun initKonfig(konfig: Konfigurable, map: Map<String, Any>): Boolean {
                val missing = konfig.items.keys - map.keys

                missing.forEach { key ->
                    val item = konfig.items[key]!!
                    item.encodeDefault(engine)
                }

                map.forEach { (key, element) ->
                    val item = konfig.items[key]!!
                    item.encoded = element

                    if(item is KonfigObjectItem<out KonfigObject>) {
                        val kObjMap = (engine as SerializationEngine<*, Any>).decodeFromElement(
                            MapSerializer(String.serializer(), engine.elementSerializer),
                            element
                        )

                        return initKonfig(item.kObj, kObjMap)
                    }
                }

                return missing.isEmpty()
            }

            if(initKonfig(this, read())) {
                write()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun write() {
        val content = engine.encodeToString(
            MapSerializer(String.serializer(), engine.elementSerializer) as KSerializer<Map<String, Any>>,
            items.mapValues { (_, item) -> item.getEncoded(engine) }
        )

        file.writeText(content)
    }

    fun read(): Map<String, Any> {
        val content = file.readText()

        return engine.decodeFromString(
            MapSerializer(String.serializer(), engine.elementSerializer),
            content
        )
    }
}