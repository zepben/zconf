/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.*
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class EnvBlobSourceProcessor @OptIn(ExperimentalForeignApi::class) constructor(
    input: String,
    private val envFetcher: (String) -> String? = { env -> getenv(env)?.toKString() }
) : SourceProcessor(input) {
    private val logger = KotlinLogging.logger {}

    @OptIn(ExperimentalEncodingApi::class)
    override fun execute(): ConfigElement {
        val envValue = envFetcher(input) ?: return ConfigObject()

        val json = try {
            val decodedValue = Base64.Default.decode(envValue)
            Json.Default.parseToJsonElement(postProcessEnv(decodedValue))
        } catch (e: Exception) {
            logger.error(e) { "Failed to decode/decompress JSON in Env $input.. skipping.." }
            return ConfigObject()
        }

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

    private fun convertToIntermediateForm(json: JsonElement, path: String, thing: ConfigObject, prevJson: JsonElement? = null) {
        when (json) {
            is JsonNull -> return // We don't care about nulls
            is JsonPrimitive -> thing.set(path.removePrefix("."), json.content, prevJson)
            is JsonArray -> json.forEachIndexed { index, jsonElement -> convertToIntermediateForm(jsonElement, "$path.$index", thing, json) }
            is JsonObject -> json.entries.forEach { (key, jsonElement) ->
                convertToIntermediateForm(
                    jsonElement,
                    "$path.${key.replace(".", "__")}", // NOTE: We do this key replacement of 'dots' with double underscores to be able to differentiate between nested objects and JSON object keys that contain 'dots' in them.
                    thing,
                    json
                )
            }
        }
    }
}
