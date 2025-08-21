/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2025 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.CompositeConfig
import com.zepben.zconf.model.ConfigArray
import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.*

open class JsonFileSourceProcessor(input: String) : SourceProcessor(input) {

    override fun execute(): ConfigElement {
        val contents = SystemFileSystem.source(Path(input)).buffered().readString()
        val json = Json.Default.decodeFromString<JsonObject>(contents)

        return convertToIntermediateForm(json)
    }

    protected fun convertToIntermediateForm(json: JsonElement): ConfigObject {
        require(json is JsonObject)

        val accumulator = ConfigObject() // just assume that we always return a full json document
        convertToIntermediateForm(json, "", accumulator)
        return accumulator
    }

    private fun convertToIntermediateForm(json: JsonElement, path: String, thing: CompositeConfig) {
        when (json) {
            is JsonNull -> return // We don't care about nulls
            is JsonPrimitive -> thing[path.removePrefix(".")] = json.content
            is JsonArray ->{
                val arr = ConfigArray().apply { thing[path] = this } // Create the ConfigArray here and don't rely on the model doing it.
                json.forEachIndexed { index, nextElement ->
                    val (newPath, obj) = if(nextElement is JsonObject)
                        "" to ConfigObject().apply { arr[index.toString()] = this } // If the nextElement is a JsonObject, we create a new ConfigObject and assign it to the array at the index.
                    else
                        index.toString() to arr

                    convertToIntermediateForm(nextElement, newPath, obj)
                }
            }
            is JsonObject -> json.entries.forEach { (key, nextElement) ->
                convertToIntermediateForm(
                    nextElement,
                    key.replace(".", "__"), // NOTE: We do this key replacement of 'dots' with double underscores to be able to differentiate between nested objects and JSON object keys that contain 'dots' in them.
                    if(nextElement is JsonObject) ConfigObject().apply { thing[key] = this } else thing // Create the ConfigObject here and don't rely on the model doing it.
                )
            }
        }
    }
}
