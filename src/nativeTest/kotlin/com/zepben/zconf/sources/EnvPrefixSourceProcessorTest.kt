/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalKotest::class)
class EnvPrefixSourceProcessorTest : FunSpec({
    context("processor execution") {
        val inputPrefix = "PREFIX"
        val fakeEnvFetcher = { input: String ->
            EnvPrefixSourceProcessor.getAllEnvsForPrefix(input, listOf(
                "PREFIX__FOO_BAR=1",
                "PREFIX__FOO_BAZ=2",
                "PREFIX__THING_0=1",
                "PREFIX__HAS__UNDERSCORE=10",
                "OTHER__PREFIX_THING=1",
                "EXCLUDED=ENV"
            ))
        }

        test("gets all properties correctly") {
            val config = EnvPrefixSourceProcessor(inputPrefix, fakeEnvFetcher).properties as ConfigObject

            config["foo.bar"] shouldBe ConfigValue("1")
            config["foo.baz"] shouldBe ConfigValue("2")
            config["thing.0"] shouldBe ConfigValue("1")
            config["has_underscore"] shouldBe ConfigValue("10")


            config["excluded"] shouldBe null
            config["prefix.thing"] shouldBe null
        }

        test("works when prefix is changed") {
            val config = EnvPrefixSourceProcessor("OTHER", fakeEnvFetcher).properties as ConfigObject

            config["prefix.thing"] shouldBe ConfigValue("1")


            config["foo.bar"] shouldBe null
            config["foo.baz"] shouldBe null
            config["thing.0"] shouldBe null
            config["has_underscore"] shouldBe null
            config["excluded"] shouldBe null
        }
    }
})
