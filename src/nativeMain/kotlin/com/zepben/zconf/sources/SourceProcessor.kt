package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement

abstract class SourceProcessor(val input: String) {

    val properties: ConfigElement by lazy { execute() }

    protected abstract fun execute(): ConfigElement
}
