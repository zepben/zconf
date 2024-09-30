/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf

import com.zepben.zconf.sources.*
import io.github.oshai.kotlinlogging.KotlinLogging

data class SourceTypeParseResult(val type: SourceType, val param: String)

enum class SourceType(val protocol: String, val sourceProcessor: (String) -> SourceProcessor) {
    NULL("null", ::NullSourceProcessor),
    ENV_BLOB_GZ("env-blob-gz", ::EnvBlobGzSourceProcessor),
    ENV_BLOB("env-blob", ::EnvBlobSourceProcessor),
    ENV_PREFIX("env-prefix", ::EnvPrefixSourceProcessor);

    companion object  {
        private const val PROTOCOL_SEPARATOR = "://"

        private val logger = KotlinLogging.logger {}

        fun parse(input: String): SourceTypeParseResult {
            val type = entries
                .toTypedArray()
                .firstOrNull { input.startsWith(it.protocol) } ?: NULL

            val arg = input.split(PROTOCOL_SEPARATOR).getOrNull(1) ?: ""

            if (!input.startsWith(type.protocol)) {
                logger.warn { "Unable to parse source type: $input. Falling back to null source" }
            }

            return SourceTypeParseResult(type, arg)
        }
    }
}
