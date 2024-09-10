package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigArray
import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.*
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class EnvBlobSourceProcessor(input: String): SourceProcessor(input) {
    @OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
    override fun execute(): ConfigElement {
        val envValue = getenv(input)?.toKString() ?: return ConfigObject()
        val decodedValue = Base64.Default.decode(envValue)
        val json = Json.Default.parseToJsonElement(postProcessEnv(decodedValue))
        return convertToIntermediateForm(json)
    }

    @OptIn(ExperimentalForeignApi::class)
    protected open fun postProcessEnv(decodedValue: ByteArray): String {
        return decodedValue.toKString()
    }

    private fun convertToIntermediateForm(json: JsonElement): ConfigObject {
        require(json is JsonObject)

        val accumulator = ConfigObject() // just assume that we always return a full json document
        convertToIntermediateForm(json, "", accumulator)
        return accumulator
    }

    private fun convertToIntermediateForm(json: JsonElement, path: String, thing: ConfigObject) {
        when (json) {
            is JsonNull -> return // We don't care about nulls
            is JsonPrimitive -> thing[path.removePrefix(".")] = json.content
            is JsonArray -> json.forEachIndexed { index, jsonElement -> convertToIntermediateForm(jsonElement, "$path.$index", thing) }
            is JsonObject -> json.entries.forEach { (key, jsonElement) -> convertToIntermediateForm(jsonElement, "$path.$key", thing) }
        }
    }
}
