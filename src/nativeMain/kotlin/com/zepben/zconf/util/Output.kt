package com.zepben.zconf.util

import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class OutputWriter {
    private val json = Json { prettyPrint = true }

    fun write(outputPath: String, contents: JsonElement) {
        val path = Path(outputPath)
        
        SystemFileSystem.sink(path).use {
            val serialJson = json.encodeToString(contents)
            val buffer = Buffer().apply {
                write(serialJson.encodeToByteArray())
                write("\n".encodeToByteArray())
            }
            it.write(buffer, buffer.size)
        }
    }
}
