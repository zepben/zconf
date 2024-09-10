package com.zepben.zconf

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import kotlinx.serialization.json.*

class Generate: CliktCommand() {

    // TODO: validation
    private val sources by option("-s", "--source").multiple()
    private val outputPath by option("-o", "--output").required()

    override fun run() {
        val parsedSources = sources.map { SourceType.parse(it) }

        val resolvedConfig = mutableMapOf<String, String>().apply {
            parsedSources.map { it.first.sourceProcessor.invoke(it.second) }.forEach {
                this.putAll(it.properties)
            }
        }

//        convertToJsonElement(resolvedConfig)

//        writeOutput(resolvedConfig)
    }
}
