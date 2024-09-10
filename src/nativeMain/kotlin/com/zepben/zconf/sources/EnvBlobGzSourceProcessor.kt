package com.zepben.zconf.sources

import com.zepben.zconf.util.Gzip
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.*
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class EnvBlobGzSourceProcessor(input: String): EnvBlobSourceProcessor(input) {
    @OptIn(ExperimentalForeignApi::class)
    override fun postProcessEnv(decodedValue: ByteArray): String {
      return Gzip.decode(decodedValue).toKString()
    }
}
