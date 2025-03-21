/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

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
})
