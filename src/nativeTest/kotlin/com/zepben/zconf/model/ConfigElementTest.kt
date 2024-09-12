/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigModelTest : FunSpec({
    var root = ConfigObject()

    beforeEach {
        root = ConfigObject()
    }

    test("returns null for nonexistent keys") {
        root["foo"] shouldBe null
        root["0"] shouldBe null
        root["foo.0"] shouldBe null
    }

    test("sets a key") {
        root["foo"] = "bar"
        root["foo"] shouldBe ConfigValue("bar")
    }

    test("sets nested yet") {
        root["foo.bar"] = "baz"
        root["foo.bar"] shouldBe ConfigValue("baz")
    }

    test("sets array based keys") {
        root["foo.0"] = "bar"
        root["foo.0"] shouldBe ConfigValue("bar")
    }

    test("sets a complex config") {
        root["auth.type"] = "entra"
        root["auth.perms.0"] = "read"
        root["auth.perms.1"] = "write"
        root["auth.other.0.key"] = "value"

        root["auth.type"] shouldBe  ConfigValue("entra")
        root["auth.perms.0"] shouldBe  ConfigValue("read")
        root["auth.perms.1"] shouldBe  ConfigValue("write")
        root["auth.other.0.key"] shouldBe  ConfigValue("value")
    }

    test("throws when setting causes structure change") {
        root["foo"] = "bar"

        shouldThrow<IllegalArgumentException> {
            root["foo.bar"] = "baz"
            Unit
        }

        shouldThrow<IllegalArgumentException> {
            root["foo.0"] = "baz"
            Unit
        }
    }

    test("throws for invalid array indexes") {
        shouldThrow<IllegalArgumentException> {
            root["foo.-1"] = "baz"
            Unit
        }
    }

    context("merging two configs") {
        test("merges like structures") {
            root["foo"] = "bar"
            root["auth.type"] = "test1"
            root["thing.0"] = "value2"

            val otherRoot = ConfigObject()
            otherRoot["foo"] = "baz"
            otherRoot["auth.type"] = "test2"
            otherRoot["thing.0"] = "value2"

            root.merge(otherRoot)

            root["foo"] shouldBe ConfigValue("baz")
            root["auth.type"] shouldBe ConfigValue("test2")
            root["thing.0"] shouldBe ConfigValue("value2")
        }

        test("merges other with less keys") {
            root["foo"] = "bar"
            root["auth.type"] = "test"

            val otherRoot = ConfigObject()
            otherRoot["auth.type"] = "test2"

            root.merge(otherRoot)

            root["foo"] shouldBe ConfigValue("bar")
            root["auth.type"] shouldBe ConfigValue("test2")
        }

        test("merges other with more keys") {
            root["foo"] = "bar"
            root["auth.type"] = "test"

            val otherRoot = ConfigObject()
            otherRoot["auth.type"] = "test2"
            otherRoot["auth.other"] = "value"

            root.merge(otherRoot)

            root["foo"] shouldBe ConfigValue("bar")
            root["auth.type"] shouldBe ConfigValue("test2")
            root["auth.other"] shouldBe ConfigValue("value")
        }

        test("does not merge other with different type") {
            root["foo"] = "bar"

            val otherRoot = ConfigObject()
            otherRoot["foo.type"] = "test2"

            shouldThrow<IllegalStateException> {
                root.merge(otherRoot)
                Unit
            }
        }
    }
})
