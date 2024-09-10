package com.zepben.zconf.sources


typealias NeutralConfig = Map<String, String>

abstract class SourceProcessor(val input: String) {

    val properties: NeutralConfig by lazy { execute() }

    protected abstract fun execute(): NeutralConfig
}
