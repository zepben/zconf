/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

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

    test ("it works on something bigger than the intermediate buffer") {
        val envVal = "H4sIAAAAAAAA/6yTz47kJhDG36WOkXEbg/9xi3aj3TkkWWnmFu0BQ9GN1g1eKE9rNOp3j/BMtyJNDjlEPlhAFUX9vq9eQW90AlX+1mMwCArEJLreCM2MdoLJ3sxslnxg0zj3UrbdIK2ACs5Ip2hBAQZK2luogNKWCe1DzhumDOovOBGtWR0OSzz6UJ+9STFHRzEsPmBt4vnAeS+k6GbWi9ExOcuBjdOEDI0ZBs7NOAh+eG7rBr5fK7Ca9Kwzlifb5J8xgYKYjvUaMx0T5p9L/fltv4JttZrw0ZzwrP8Mj6QTgaK04bUCvMxvbX9o33DHZWMdm4bWMKndxMYGOZNNN3Ixi9k0LVRgFo+BHux/B+Z3LKD+Vyj/osO1glPMVHYucx2QLjH9OEeLSx1iWFO07Hk1tb7k2geCCtZYwEgpKlhTpGjiAgq+Pj19e4QKEv7cMNPvtzJffnuCCp4xeffyCRN5542mQs7pJWMp78MRM8VUyKYtkD/vghGe12UPfQWdsz+Gb9u8ePOwgtpzC9XioPSHLhmAJrP397O9AVZkq8DEQNqHe9y9XgUZzZY8vXxJcVsf7G7CfGRNx6ceh9bOUzP1AxfwvYK8zQHpFrQvWCOmYRSis1r3vJOmh+p+1KEd3dS53lnnumn6x5HtzdS5buZtN8wD1+V60vnHZ3Q+ePIx/JoCKNApKH3JCk1WemU5bnRCnYm1auz40HZSTqPgquQye08+fKDAbj0rWQbvZX3nVfC/r+6U4HotYNI+La83czT1/t31H/pOVnDB+dPu7K9vUb/A9fo3AAAA//8BAAD//w/eRjwmBAAA"
        val config = EnvBlobGzSourceProcessor("FAKE") { _ -> envVal }.properties as ConfigObject

        config["server.host"] shouldBe ConfigValue("0.0.0.0")
    }
})
