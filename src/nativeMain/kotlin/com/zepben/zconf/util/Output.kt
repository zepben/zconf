/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.util

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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
