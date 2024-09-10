package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject

class NullSourceProcessor(input: String): SourceProcessor(input) {

    override fun execute(): ConfigElement {
        // intentional noop
        return ConfigObject()
    }
}
