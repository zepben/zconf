/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.util

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class OutputWriter {
    private val json = Json { prettyPrint = true }

    fun write(outputPath: String, contents: JsonElement) {
        try {
            val path = Path(outputPath)

            SystemFileSystem.sink(path).use {
                val serialJson = json.encodeToString(contents)
                val buffer = Buffer().apply {
                    write(serialJson.encodeToByteArray())
                    write("\n".encodeToByteArray())
                }
                it.write(buffer, buffer.size)
            }
        } catch (e: IOException) {

        }
    }
}
