package com.zepben.zconf.util

import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class OutputWriter {
    fun write(path: String, contents: JsonElement) {
        val resolvedPath = SystemFileSystem.resolve(Path(path))
        SystemFileSystem.sink(resolvedPath).use {
            val stringifiedJson = Json.Default.encodeToString(contents)
            val buffer = Buffer()
            buffer.write(stringifiedJson.encodeToByteArray())
            it.write(buffer, buffer.size)
        }

    }
}
