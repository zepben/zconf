package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.zepben.zconf.sources.EnvBlobSourceProcessor
import com.zepben.zconf.util.Gzip
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Main: CliktCommand(name = "zconf") {
    override fun run() {
        // Noop
    }
}

fun main(args: Array<String>) = Main().subcommands(Version(), Generate()).main(args)
