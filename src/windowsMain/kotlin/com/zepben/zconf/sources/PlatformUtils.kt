/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import platform.posix.NULL
import platform.posix._environ

internal actual object PlatformUtils {
    @OptIn(ExperimentalForeignApi::class)
    actual fun getAllEnvs(): List<String> {
        var index = 0
        val envs = mutableListOf<String>()
        while (_environ?.get(index) != NULL) {
            val env = _environ?.get(index)?.toKString() ?: throw IllegalStateException("Should never be null")
            envs.add(env)
            index += 1
        }
        return envs
    }
}
