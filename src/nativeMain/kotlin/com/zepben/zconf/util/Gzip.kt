/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.util

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import platform.zlib.*

class Gzip {
    companion object {
        private val logger = KotlinLogging.logger {}

        private const val BUFFER_MAX = 1024
        @OptIn(ExperimentalForeignApi::class)
        fun decode(compressed: ByteArray): ByteArray {
            memScoped {
                val stream = alloc<z_stream>()

                if(inflateInit2(stream.ptr, 15 + 16) != Z_OK) {
                    throw IllegalStateException("Unable to initialize Zlib")
                }

                stream.next_in = compressed.usePinned { it.addressOf(0).reinterpret() }
                stream.avail_in = compressed.size.toUInt()

                var ret = Z_OK
                var intermediate = ByteArray(BUFFER_MAX)
                val output = Buffer()

                do {
                    stream.next_out = intermediate.usePinned { it.addressOf(0).reinterpret() }
                    stream.avail_out = intermediate.size.toUInt()

                    ret = inflate(stream.ptr, Z_FINISH)

                    output.write(intermediate)
                    intermediate = ByteArray(BUFFER_MAX)
                } while (ret == Z_BUF_ERROR)

                if (ret < 0) {
                    logger.error { "Failed to decompress, inflate returned $ret" }
                }

                inflateEnd(stream.ptr)

                return output.readByteArray(stream.total_out.toInt())
            }
        }
    }
}

