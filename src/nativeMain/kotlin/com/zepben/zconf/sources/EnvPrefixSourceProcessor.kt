/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.sources

import com.zepben.zconf.model.ConfigElement
import com.zepben.zconf.model.ConfigObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import platform.posix.NULL
import platform.posix.__environ

class EnvPrefixSourceProcessor(
    input: String,
    private val envsFetcher: (String) -> Map<String, String> = { prefix -> getAllEnvsForPrefix(prefix) }
) : SourceProcessor(input) {

    companion object {
        fun getAllEnvsForPrefix(prefix: String, envs: List<String> = getAllEnvs()): Map<String, String> {
            return envs.map { it.split("=") }
                .associate {
                    // preserve any envs with an equal sign in it
                    it[0] to it.subList(1, it.size).joinToString("=")
                }
                .filter { (k, _) ->
                    k.startsWith("${prefix}__")
                }
        }

        @OptIn(ExperimentalForeignApi::class)
        private fun getAllEnvs(
        ): MutableList<String> {
            var index = 0
            val envs = mutableListOf<String>()

            while (__environ?.get(index) != NULL) {
                val env = __environ?.get(index)?.toKString() ?: throw IllegalStateException("Should never be null")
                envs.add(env)
                index += 1
            }

            return envs
        }
    }

    override fun execute(): ConfigElement {
        val config = ConfigObject()
        val envs = envsFetcher(input)

        envs.forEach { (key , value) ->
            val resolvedKey = key.removePrefix("${input}__")
                .replace("__", "$") // escape double underscores
                .replace("_", ".") // replace underscores for dots
                .replace("$", "_") // put the escaped underscores back

            config[resolvedKey] = value
        }

        return config
    }
}
