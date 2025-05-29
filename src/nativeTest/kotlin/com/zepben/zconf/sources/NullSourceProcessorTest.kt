/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.jsonObject

class NullSourceProcessorTest : FunSpec({
    test("it returns an empty config object") {
        val source = NullSourceProcessor()
        source.properties.toJson().jsonObject.size shouldBe 0
    }

    test("it ignores the constructor parameter") {
        val source = NullSourceProcessor("param")
        source.properties.toJson().jsonObject.size shouldBe 0
    }
})
