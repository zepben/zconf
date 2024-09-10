package com.zepben.zconf.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getenv

class Env {
    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun get(envar: String) = getenv(envar) ?: ""
    }
}
