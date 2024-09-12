/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.util.OutputWriter

class Generate: CliktCommand() {

    // TODO: validation
    private val sources by option("-s", "--source").multiple()
    private val outputPath by option("-o", "--output").required()

    override fun run() {
        val config = ConfigObject()

        sources.map { SourceType.parse(it) }
            .map { it.type.sourceProcessor.invoke(it.param) }
            .forEach { config.merge(it.properties) }

        OutputWriter().write(outputPath, config.toJson())
    }
}
