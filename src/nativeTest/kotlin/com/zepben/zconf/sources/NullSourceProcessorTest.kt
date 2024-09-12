/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
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
