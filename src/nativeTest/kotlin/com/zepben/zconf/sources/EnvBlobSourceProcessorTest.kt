/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class EnvBlobSourceProcessorTest : FunSpec({
    test("handles non existent environment variables") {
        EnvBlobSourceProcessor("DEFINITELY_NOT_A_REAL_ENV_VAR").properties
    }

    test("handles non json environment variables") {
        EnvBlobSourceProcessor("FAKE") { _ -> "" }.properties
    }

    //{
    //    "menu": {
    //        "id": "file",
    //        "value": "File",
    //        "popup": {
    //            "menuitem": [
    //                {
    //                    "value": "New",
    //                    "onclick": "CreateNewDoc()"
    //                },
    //                {
    //                    "value": "Open",
    //                    "onclick": "OpenDoc()"
    //                },
    //                {
    //                    "value": "Close",
    //                    "onclick": "CloseDoc()"
    //                }
    //            ]
    //        }
    //    }
    //}

    test("parses a complex JSON var") {
        val envVal = "eyJtZW51IjogewogICJpZCI6ICJmaWxlIiwKICAidmFsdWUiOiAiRmlsZSIsCiAgInBvcHVwIjogewogICAgIm1lbnVpdGVtIjogWwogICAgICB7InZhbHVlIjogIk5ldyIsICJvbmNsaWNrIjogIkNyZWF0ZU5ld0RvYygpIn0sCiAgICAgIHsidmFsdWUiOiAiT3BlbiIsICJvbmNsaWNrIjogIk9wZW5Eb2MoKSJ9LAogICAgICB7InZhbHVlIjogIkNsb3NlIiwgIm9uY2xpY2siOiAiQ2xvc2VEb2MoKSJ9CiAgICBdCiAgfQp9fQ=="
        val config = EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        config["menu.id"] shouldBe ConfigValue("file")
        config["menu.value"] shouldBe ConfigValue("File")
        config["menu.popup.menuitem.0.value"] shouldBe ConfigValue("New")
    }

    test("safely fails for invalid b64 data") {
        val envVal = "eyJtZW51IjogewogICJpZCI6ICJmaWxlIiwKICAidmFsdWUiOiAiRmlsZSIsCiAgInBvcH"
        EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties
    }

    test("preserves json element when parsing") {
        val envVal = "ewogICJpZCI6IDEsCiAgIm1hcHBpbmdzIjogewogICAgIjIzMCI6NDAwLAogICAgIjI0MCI6NDE1LAogICAgIjI1MCI6NDMzLAogICAgIjYzNTAiOjExMDAwLAogICAgIjY0MDAiOjExMDAwLAogICAgIjEyNzAwIjoyMjAwMCwKICAgICIxOTEwMCI6MzMwMDAKICB9Cn0="
        val config = EnvBlobSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        Json.encodeToString(config.toJson()) shouldBe """{"id":"1","mappings":{"230":"400","240":"415","250":"433","6350":"11000","6400":"11000","12700":"22000","19100":"33000"}}"""
    }
})
