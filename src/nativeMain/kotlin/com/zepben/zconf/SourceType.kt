package com.zepben.zconf

import com.zepben.zconf.sources.EnvBlobGzSourceProcessor
import com.zepben.zconf.sources.EnvBlobSourceProcessor
import com.zepben.zconf.sources.NullSourceProcessor
import com.zepben.zconf.sources.SourceProcessor

data class SourceTypeParseResult(val type: SourceType, val param: String)

enum class SourceType(val protocol: String, val sourceProcessor: (String) -> SourceProcessor) {
    NULL("null", ::NullSourceProcessor),
    ENV_BLOB_GZ("env-blob-gz", ::EnvBlobGzSourceProcessor),
    ENV_BLOB("env-blob", ::EnvBlobSourceProcessor);

    companion object  {
        private const val PROTOCOL_SEPARATOR = "://"

        fun parse(input: String): SourceTypeParseResult {
            val type = entries
                .toTypedArray()
                .firstOrNull { input.startsWith(it.protocol) } ?: NULL

            val arg = input.split(PROTOCOL_SEPARATOR).getOrNull(1) ?: ""

            return SourceTypeParseResult(type, arg)
        }
    }
}
