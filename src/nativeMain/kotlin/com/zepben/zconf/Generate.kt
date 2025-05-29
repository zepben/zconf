/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.*
import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.util.OutputWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.exit
import platform.posix.getenv

class Generate: CliktCommand() {
    init {
        context {
            autoEnvvarPrefix = "ZCONF"
        }
    }

    private val sourceEnv by option("--source-env").default("")
    private val sources by option("-s", "--source").multiple().unique()
    private val outputPath by option("-o", "--output").required()

    private val logger = KotlinLogging.logger {}

    @OptIn(ExperimentalForeignApi::class)
    override fun run() {
        val config = ConfigObject()
        val resolvedSources = getenv(sourceEnv)?.toKString()?.split(",") ?: sources

        if (resolvedSources.isEmpty()) {
            logger.error { "No sources specified, exiting" }
            exit(1)
        }

        resolvedSources.map { SourceType.parse(it) }
            .map { it.type.sourceProcessor.invoke(it.param) }
            .forEach { config.merge(it.properties) }

        OutputWriter().write(outputPath, config.toJson())
    }
}
