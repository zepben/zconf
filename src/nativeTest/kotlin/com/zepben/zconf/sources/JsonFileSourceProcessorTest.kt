/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.*
import platform.posix.getcwd

@OptIn(ExperimentalForeignApi::class)
class JsonFileSourceProcessorTest : FunSpec({

    val currentWorkingDir = memScoped {
        val tmp = allocArray<ByteVar>(512)
        getcwd(tmp, 512.convert())
        tmp.toKString()
    }

    test("handles non existent file") {
        JsonFileSourceProcessor("$currentWorkingDir/fake/directory/fake.json").properties
    }

    test("parses a complex JSON var") {
        val config = JsonFileSourceProcessor("$currentWorkingDir/src/nativeTest/resources/fixtures/sample.json").properties as ConfigObject

        config["menu.id"] shouldBe ConfigValue("file")
        config["menu.value"] shouldBe ConfigValue("File")
        config["menu.popup.menuitem.0.value"] shouldBe ConfigValue("New")
        config["menu.popup.v1__kubernetes__zepben__com/node-class"] shouldBe ConfigValue("high-memory")
    }
})
