/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import platform.Foundation.NSProcessInfo

internal actual object PlatformUtils {
    actual fun getAllEnvs(): List<String> {
        return NSProcessInfo.processInfo.environment.map { (key, value) -> "$key=$value" }
    }
}
