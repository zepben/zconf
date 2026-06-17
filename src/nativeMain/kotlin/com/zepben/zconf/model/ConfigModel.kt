/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.zconf.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ConfigElement {
    protected fun parsePath(path: String): Pair<String, String> {
        val keys = path.split(".")
        val key = keys.first().replace(
            "__",
            "."
        ) // NOTE: We do this key replacement of 'dots' with double underscores to be able to differentiate between nested objects and JSON object keys that contain 'dots' in them.
        val rest = keys.drop(1).joinToString(".")

        return Pair(key, rest)
    }

    fun toJson(): JsonElement {
        return when (this) {
            is ConfigArray -> JsonArray(this.contents().map { it.toJson() })
            is ConfigObject -> JsonObject(this.contents().map { it.key to it.value.toJson() }.toMap())
            is ConfigValue<*> -> when (this.value) {
                is Boolean -> JsonPrimitive(this.value)
                is Number -> JsonPrimitive(this.value)
                else -> JsonPrimitive(this.value.toString())
            }
        }
    }

    protected fun <T> createArrayOrObject(rest: String, value: T?): ConfigElement {
        val (subKey, _) = parsePath(rest)
        val subIndex = subKey.toIntOrNull()

        val newElement = if (subIndex != null) {
            validateIndex(subIndex)
            // if the next key isn't parsable as an int, then its an array
            ConfigArray().apply {
                this[rest] = value
            }
        } else {
            ConfigObject().apply {
                this[rest] = value
            }
        }
        return newElement
    }

    abstract fun merge(other: ConfigElement?): ConfigElement
}

interface CompositeConfig{
    operator fun set(path: String, value: ConfigElement)
    operator fun <T> set(path: String, value: T?)
    operator fun get(path: String): ConfigElement?
}

data class ConfigValue<T>(val value: T?) : ConfigElement() {

    override fun merge(other: ConfigElement?): ConfigElement {
        when (other) {
            is ConfigValue<*> -> return ConfigValue(other.value)
            null -> return this
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }

    override fun toString(): String {
        return "ConfigValue(type: ${if (value !== null) value::class.simpleName else "null"}, value: $value)"
    }
}

class ConfigObject(private val contents: MutableMap<String, ConfigElement> = mutableMapOf()) : ConfigElement(), CompositeConfig {

    fun contents() = contents.toMap()
    override operator fun get(path: String): ConfigElement? {
        val (key, rest) = parsePath(path)

        return when (val node = contents[key]) {
            is ConfigArray -> node[rest]
            is ConfigObject -> node[rest]
            is ConfigValue<*> -> node
            null -> null
        }
    }

    override operator fun set(path: String, value: ConfigElement) {
        require(parsePath(path).second.isEmpty()) { "Cannot set a ConfigElement for multilevel $path, set a value instead" }
        contents[path] = value
    }

    override operator fun <T> set(path: String, value: T?) {
        val (key, rest) = parsePath(path)

        when (val element = contents[key]) {
            is ConfigArray -> element[rest] = value
            is ConfigObject -> element[rest] = value
            is ConfigValue<*> -> {
                require(rest.isEmpty()) { "Attempting to set $value at $path, but element not traversable" }
                contents[key] = ConfigValue(value)
            }

            null -> {
                handleNullSet(rest, key, value)
            }
        }

    }

    private fun <T> handleNullSet(rest: String, key: String, value: T?) {
        if (rest.isEmpty()) {
            // if rest is empty we are ready to set a value
            contents[key] = ConfigValue(value)
        } else {
            val newElement = createArrayOrObject(rest, value)

            contents[key] = newElement
        }
    }

    override fun merge(other: ConfigElement?): ConfigObject {
        when (other) {
            is ConfigObject -> {
                val newContents = contents.toMutableMap()
                val allKeys = contents.keys + other.contents.keys
                allKeys.forEach { key ->
                    val currentValue = contents[key]
                    val otherValue = other.contents[key]

                    if (currentValue != null && otherValue != null) {
                        newContents[key] = currentValue.merge(otherValue)
                    } else if (currentValue != null) {
                        return@forEach
                    } else if (otherValue != null) {
                        newContents[key] = otherValue
                    }
                }
                return ConfigObject(newContents)
            }

            null -> return this
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }
}

class ConfigArray(private val contents: MutableMap<String, ConfigElement> = mutableMapOf()) : ConfigElement(), CompositeConfig {
    fun contents() = contents.values.toList()

    override operator fun get(path: String): ConfigElement? {
        val (key, rest) = parsePath(path)
        val index = key.toIntOrNull()

        validateIndex(index)

        return when (val node = contents[index.toString()]) {
            is ConfigArray -> node[rest]
            is ConfigObject -> node[rest]
            is ConfigValue<*> -> node
            null -> null
        }
    }

    override operator fun set(path: String, value: ConfigElement) {
        require(parsePath(path).second.isEmpty()) { "Cannot set a ConfigElement for multilevel $path, set a value instead" }
        contents[path] = value
    }

    override operator fun <T> set(path: String, value: T?) {
        val (key, rest) = parsePath(path)
        val index = key.toIntOrNull()

        validateIndex(index)

        when (val element = contents[index.toString()]) {
            is ConfigArray -> element[rest] = value
            is ConfigObject -> element[rest] = value
            is ConfigValue<*> -> {
                require(rest.isEmpty()) { "Attempting to set $value at $path, but element not traversable" }
                contents[index.toString()] = ConfigValue(value)
            }

            null -> handleNullSet(rest, index, value)
        }
    }


    override fun merge(other: ConfigElement?): ConfigArray {
        when (other) {
            is ConfigArray -> {
                val newContents = contents.toMutableMap()
                val allKeys = contents.keys + other.contents.keys
                allKeys.forEach { key ->
                    val currentValue = contents[key]
                    val otherValue = other.contents[key]

                    if (currentValue != null && otherValue != null) {
                        newContents[key] = currentValue.merge(otherValue)
                    } else if (currentValue != null) {
                        return@forEach
                    } else if (otherValue != null) {
                        newContents[key] = otherValue
                    }
                }
                return ConfigArray(newContents)
            }

            null -> return this
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }

    private fun <T> handleNullSet(rest: String, index: Int, value: T?) {
        if (rest.isEmpty()) {
            // if rest is empty we are ready to set a value
            contents[index.toString()] = ConfigValue(value)
        } else {
            val newElement = createArrayOrObject(rest, value)

            contents[index.toString()] = newElement
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun validateIndex(index: Int?) {
    contract {
        returns() implies (index != null)
    }

    require(index != null) { "Array node present but next path item is not index" }
    require(index >= 0) { "Array node must not index negative" }
}
