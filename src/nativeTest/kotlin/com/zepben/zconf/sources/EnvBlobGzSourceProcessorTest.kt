package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigObject
import com.zepben.zconf.model.ConfigValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnvBlobGzSourceProcessorTest : FunSpec({
    test("handles non existent environment variables") {
        EnvBlobGzSourceProcessor("DEFINITELY_NOT_A_REAL_ENV_VAR").properties
    }

    test("handles non json environment variables") {
        EnvBlobGzSourceProcessor("FAKE") { _ -> "" }.properties
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
        val envVal = "H4sIAAAAAAAAA6tWyk3NK1WyUqjmUlBQykwBspTSMnNSlXRA/LLEnNJUkJAbXKggv6C0AKoeyAXpzixJzQWKRINFFBSqEdr8UsuVdBSU8vOSczKTs0EizkWpiSWpQHGX/GQNTaVaHUxN/gWpeWi6QEK4NTjn5BenotsDEoNqAeuIBZK1XLW1AKbny7HxAAAA"
        val config = EnvBlobGzSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        config["menu.id"] shouldBe ConfigValue("file")
        config["menu.value"] shouldBe ConfigValue("File")
        config["menu.popup.menuitem.0.value"] shouldBe ConfigValue("New")
    }

    test("safely fails for invalid gzip-ed data") {
        val envVal = "H4sIAAAAAAAAA6tWyk3NK1WyUqjmUlBQykwBspTSMnNSlXRA/"
        EnvBlobGzSourceProcessor("FAKE") { _ -> envVal }.properties
    }
})
