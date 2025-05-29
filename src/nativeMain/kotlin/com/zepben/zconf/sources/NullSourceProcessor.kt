/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
