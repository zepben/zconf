/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject

class NullSourceProcessor(input: String = ""): SourceProcessor(input) {

    override fun execute(): ConfigElement {
        // intentional noop
        return ConfigObject()
    }
}
