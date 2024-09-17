/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SourceTypeTest : FunSpec({
    test("parses env-blob-gz correctly") {
        val type = SourceType.parse("env-blob-gz://ENV")

        type.type shouldBe SourceType.ENV_BLOB_GZ
        type.param shouldBe "ENV"
    }

    test("parses env-blob correctly") {
        val type = SourceType.parse("env-blob://ENV")

        type.type shouldBe SourceType.ENV_BLOB
        type.param shouldBe "ENV"
    }

    test("parses null correctly") {
        val type = SourceType.parse("null://ENV")

        type.type shouldBe SourceType.NULL
        type.param shouldBe "ENV"
    }

    test("falls back to null if unknown") {
        val type = SourceType.parse("fake://ENV")

        type.type shouldBe SourceType.NULL
        type.param shouldBe "ENV"
    }
})
