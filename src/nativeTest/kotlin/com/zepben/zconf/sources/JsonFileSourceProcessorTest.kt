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
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.getcwd

@OptIn(ExperimentalForeignApi::class)
class JsonFileSourceProcessorTest : FunSpec({

    val currentWorkingDir = memScoped {
        val tmp = allocArray<ByteVar>(512)
        getcwd(tmp, 512.convert())
        tmp.toKString()
    }

    val fixtures = "$currentWorkingDir/src/nativeTest/resources/fixtures"
    val optionalRefFixtures = "$fixtures/optional-ref"
    val runtimeDir = "$currentWorkingDir/build/zconf-test-fixtures"

    fun fileRef(absolutePath: String) = "file://$absolutePath"

    fun configWithOptionalRef(
        refPath: String,
        key: String = "metricsDatabase",
        siblings: String = """"app": { "name": "parent" }""",
    ) = """
        {
          ${if (siblings.isNotEmpty()) "$siblings," else ""}
          "$key": { "$OPTIONAL_REF": "file://$refPath" }
        }
    """.trimIndent()

    fun writeRuntimeConfig(name: String, json: String): String {
        val path = Path("$runtimeDir/$name")
        path.parent?.let { SystemFileSystem.createDirectories(it) }
        SystemFileSystem.sink(path).use { sink ->
            val buffer = Buffer().apply { write(json.encodeToByteArray()) }
            sink.write(buffer, buffer.size)
        }
        return path.toString()
    }

    fun loadConfig(mainPath: String): ConfigObject =
        JsonFileSourceProcessor(mainPath).properties as ConfigObject

    fun ConfigObject.shouldBeEmpty() {
        contents().isEmpty() shouldBe true
    }

    fun ConfigObject.shouldHavePaths(expected: Map<String, ConfigValue<*>?>) {
        expected.forEach { (path, value) -> this[path] shouldBe value }
    }

    context("basic file loading") {
        test("handles non existent file") {
            loadConfig("$currentWorkingDir/fake/directory/fake.json").shouldBeEmpty()
        }

        test("parses a complex JSON var") {
            loadConfig("$fixtures/sample.json").shouldHavePaths(
                mapOf(
                    "menu.id" to ConfigValue("file"),
                    "menu.value" to ConfigValue("File"),
                    "menu.popup.menuitem.0.value" to ConfigValue("New"),
                    "menu.popup.v1__kubernetes__zepben__com/node-class" to ConfigValue("high-memory"),
                    "menu.numberThree" to ConfigValue(3L),
                    "menu.numberThreeString" to ConfigValue("3"),
                    "menu.pi" to ConfigValue(3.14),
                    "menu.piString" to ConfigValue("3.14"),
                    "menu.isTrue" to ConfigValue(true),
                    "menu.isTrueString" to ConfigValue("true"),
                ),
            )
        }
    }

    context("\$optionalRef") {
        test("inlines optional ref when target exists") {
            val config = loadConfig(
                writeRuntimeConfig(
                    "present-ref.json",
                    configWithOptionalRef("$optionalRefFixtures/ref-target.json"),
                ),
            )

            config.shouldHavePaths(
                mapOf(
                    "app.name" to ConfigValue("parent"),
                    "metricsDatabase.host" to ConfigValue("db.example.com"),
                    "metricsDatabase.port" to ConfigValue(5432L),
                ),
            )
        }

        test("omits key when optional ref target is missing") {
            val config = loadConfig(
                writeRuntimeConfig(
                    "missing-ref.json",
                    configWithOptionalRef("$optionalRefFixtures/does-not-exist.json"),
                ),
            )

            config.shouldHavePaths(
                mapOf(
                    "app.name" to ConfigValue("parent"),
                    "metricsDatabase.host" to null,
                    "metricsDatabase.port" to null,
                ),
            )
        }

        test("returns empty config when ref target has invalid JSON") {
            loadConfig(
                writeRuntimeConfig(
                    "invalid-ref.json",
                    configWithOptionalRef("$optionalRefFixtures/invalid-target.json", siblings = ""),
                ),
            ).shouldBeEmpty()
        }

        test("resolves optional ref that points to another optional ref") {
            // leaf:   { "deep": "value" }
            // middle: { "next": { "$optionalRef": "file://.../ref-leaf.json" } }
            // main:   { "chain": { "$optionalRef": "file://.../middle-optional-ref.json" } }

            val leafFixture = "$optionalRefFixtures/ref-leaf.json"
            val middleConfig = writeRuntimeConfig(
                "middle-optional-ref.json",
                configWithOptionalRef(leafFixture, key = "next", siblings = ""),
            )
            val mainConfig = writeRuntimeConfig(
                "main-optional-ref.json",
                configWithOptionalRef(middleConfig, key = "chain", siblings = ""),
            )

            loadConfig(mainConfig).shouldHavePaths(
                mapOf("chain.next.deep" to ConfigValue("value")),
            )
        }

        test("removes array element when optional ref target is missing") {
            val config = loadConfig(
                writeRuntimeConfig(
                    "array-missing-ref.json",
                    """
                    {
                      "items": [
                        { "$OPTIONAL_REF": "${fileRef("$optionalRefFixtures/array-ref-target.json")}" },
                        { "$OPTIONAL_REF": "${fileRef("$optionalRefFixtures/missing.json")}" },
                        { "static": true }
                      ]
                    }
                    """.trimIndent(),
                ),
            )

            config.shouldHavePaths(
                mapOf(
                    "items.0.inlined" to ConfigValue(true),
                    "items.1.static" to ConfigValue(true),
                    "items.2.static" to null, // Actually removed missing.json ref
                ),
            )
        }

        test("returns empty config when optional ref uses non-file scheme") {
            loadConfig(
                writeRuntimeConfig(
                    "bad-scheme.json",
                    """
                    {
                      "metricsDatabase": {
                        "$OPTIONAL_REF": "http://example.com/config.json"
                      }
                    }
                    """.trimIndent(),
                ),
            ).shouldBeEmpty()
        }

        test("resolves root document from optional ref when target exists") {
            val config = loadConfig(
                writeRuntimeConfig(
                    "root-present-ref.json",
                    """
                    {
                      "$OPTIONAL_REF": "${fileRef("$optionalRefFixtures/ref-target.json")}"
                    }
                    """.trimIndent(),
                ),
            )

            config.shouldHavePaths(
                mapOf(
                    "host" to ConfigValue("db.example.com"),
                    "port" to ConfigValue(5432L),
                ),
            )
        }

        test("returns empty config when root optional ref target is missing") {
            loadConfig(
                writeRuntimeConfig(
                    "root-missing-ref.json",
                    """
                    {
                      "$OPTIONAL_REF": "${fileRef("$optionalRefFixtures/does-not-exist.json")}"
                    }
                    """.trimIndent(),
                ),
            ).shouldBeEmpty()
        }
    }
})
