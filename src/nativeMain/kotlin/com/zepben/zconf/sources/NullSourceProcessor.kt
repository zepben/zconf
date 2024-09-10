package com.zepben.zconf.sources

class NullSourceProcessor(input: String): SourceProcessor(input) {

    override fun execute(): NeutralConfig {
        // intentional noop
        return mapOf()
    }
}
