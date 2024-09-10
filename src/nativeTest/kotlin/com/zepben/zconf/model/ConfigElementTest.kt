package com.zepben.zconf.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigModelTest : FunSpec({
    var root = ConfigObject()

    beforeEach {
        root = ConfigObject()
    }

    test("fetching non existent element returns null") {
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
})
