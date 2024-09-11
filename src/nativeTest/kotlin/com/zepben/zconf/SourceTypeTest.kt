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
        val type = SourceType.parse("null://ENV")

        type.type shouldBe SourceType.NULL
        type.param shouldBe "ENV"
    }
})
