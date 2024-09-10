package com.zepben.zconf.sources

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.*
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class EnvBlobSourceProcessor(input: String): SourceProcessor(input) {
    @OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
    override fun execute(): NeutralConfig {
        val envValue = getenv(input)?.toKString() ?: return mapOf()
        val decodedValue = Base64.Default.decode(envValue)
        val json = Json.Default.parseToJsonElement(postProcessEnv(decodedValue))
        return convertToIntermediateForm(json)
    }

    @OptIn(ExperimentalForeignApi::class)
    protected open fun postProcessEnv(decodedValue: ByteArray): String {
        return decodedValue.toKString()
    }

    private fun convertToIntermediateForm(json: JsonElement): Map<String, String> {
        val accumulator = mutableMapOf<String, String>()
        convertToIntermediateForm(json, "", accumulator)
        return accumulator.toMap()
    }

    private fun convertToIntermediateForm(json: JsonElement, path: String, thing: MutableMap<String, String>) {
        when (json) {
            is JsonNull -> return // We don't care about nulls
            is JsonPrimitive -> thing[path.removePrefix(".")] = json.content
            is JsonArray -> json.forEachIndexed { index, jsonElement -> convertToIntermediateForm(jsonElement, "$path.$index", thing) }
            is JsonObject -> json.entries.forEach { (key, jsonElement) -> convertToIntermediateForm(jsonElement, "$path.$key", thing) }
        }
    }
}
