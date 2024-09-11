package com.zepben.zconf.sources

import com.zepben.zconf.util.Gzip
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.*
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
