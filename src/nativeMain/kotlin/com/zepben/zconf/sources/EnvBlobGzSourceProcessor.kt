/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
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
