/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement

abstract class SourceProcessor(val input: String) {

    val properties: ConfigElement by lazy { execute() }

    protected abstract fun execute(): ConfigElement
}
