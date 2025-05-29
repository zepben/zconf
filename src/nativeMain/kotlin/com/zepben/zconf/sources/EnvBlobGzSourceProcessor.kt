/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.util.Gzip
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

open class EnvBlobGzSourceProcessor @OptIn(ExperimentalForeignApi::class) constructor(
    input: String,
    envFetcher: (String) -> String? = { env -> getenv(env)?.toKString() }
): EnvBlobSourceProcessor(input, envFetcher) {
    @OptIn(ExperimentalForeignApi::class)
    override fun postProcessEnv(decodedValue: ByteArray): String {
        return try {
            Gzip.decode(decodedValue).toKString()
        } catch (e: Exception) {
            ""
        }
    }
}
