/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigArray
import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.CompositeConfig
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
) : JsonFileSourceProcessor(input) {
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
}
