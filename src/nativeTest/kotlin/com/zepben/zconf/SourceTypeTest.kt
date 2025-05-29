/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
