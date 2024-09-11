package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.util.OutputWriter
import kotlinx.serialization.json.*

class Generate: CliktCommand() {

    // TODO: validation
    private val sources by option("-s", "--source").multiple()
    private val outputPath by option("-o", "--output").required()

    override fun run() {
        val config = ConfigObject()

        sources.map { SourceType.parse(it) }
            .map { it.first.sourceProcessor.invoke(it.second) }
            .forEach { config.merge(it.properties) }

        OutputWriter().write(outputPath, config.toJson())
    }
}
