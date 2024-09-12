/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

package com.zepben.zconf.model

import kotlinx.serialization.json.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class ConfigElement {
    protected fun parsePath(path: String): Pair<String, String> {
        val keys = path.split(".")
        val key = keys.first()
        val rest = keys.drop(1).joinToString(".")

        return Pair(key, rest)
    }

    fun toJson(): JsonElement {
        return when (this) {
            is ConfigArray -> JsonArray(this.contents().map { it.toJson() })
            is ConfigObject -> JsonObject(this.contents().map { it.key to it.value.toJson() }.toMap())
            is ConfigValue -> JsonPrimitive(this.value)
        }
    }

    protected fun createArrayOrObject(rest: String, value: String?): ConfigElement {
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

    abstract fun merge(other: ConfigElement?)
}

data class ConfigValue(var value: String?): ConfigElement() {

    override fun merge(other: ConfigElement?) {
        when (other) {
            is ConfigValue -> value = other.value
            null -> return
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }
}

class ConfigObject(private val contents: MutableMap<String, ConfigElement> = mutableMapOf()): ConfigElement() {

    fun contents() = contents.toMap()
    operator fun get(path: String): ConfigElement? {
        val (key, rest) = parsePath(path)

        return when(val node = contents[key]) {
            is ConfigArray -> node[rest]
            is ConfigObject -> node[rest]
            is ConfigValue -> node
            null -> null
        }
    }

    operator fun set(path: String, value: String?) {
        val (key, rest) = parsePath(path)

        when (val element = contents[key]) {
            is ConfigArray -> element[rest] = value
            is ConfigObject -> element[rest] = value
            is ConfigValue -> {
                require(rest.isEmpty()) { "Attempting to set $value at $path, but element not traversable"}
                element.value = value
            }
            null -> {
                handleNullSet(rest, key, value)
            }
        }

    }

    private fun handleNullSet(rest: String, key: String, value: String?) {
        if (rest.isEmpty()) {
            // if rest is empty we are ready to set a value
            contents[key] = ConfigValue(value)
        } else {
            val newElement = createArrayOrObject(rest, value)

            contents[key] = newElement
        }
    }

    override fun merge(other: ConfigElement?) {
        when (other) {
            is ConfigObject -> {
                val allKeys = contents.keys + other.contents.keys
                allKeys.forEach { key ->
                    val currentValue = contents[key]
                    val otherValue = other.contents[key]

                    if(currentValue != null && otherValue != null) {
                        currentValue.merge(otherValue)
                    } else if (currentValue != null) {
                        return@forEach
                    } else if (otherValue != null) {
                        contents[key] = otherValue
                    }
                }
            }
            null -> return
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }
}

class ConfigArray(private val contents: MutableMap<String, ConfigElement> = mutableMapOf()): ConfigElement() {
    fun contents() = contents.values.toList()

    operator fun get(path: String): ConfigElement? {
        val (key, rest) = parsePath(path)
        val index = key.toIntOrNull()

        validateIndex(index)

         return when (val node = contents[index.toString()]) {
             is ConfigArray -> node[rest]
             is ConfigObject -> node[rest]
             is ConfigValue -> node
             null -> null
         }
    }

    operator fun set(path: String, value: String?) {
        val (key, rest) = parsePath(path)
        val index = key.toIntOrNull()

        validateIndex(index)

        when (val element = contents[index.toString()]) {
            is ConfigArray -> element[rest] = value
            is ConfigObject -> element[rest] = value
            is ConfigValue -> {
                require(rest.isEmpty()) { "Attempting to set $value at $path, but element not traversable"}
                element.value = value
            }

            null -> handleNullSet(rest, index, value)
        }
    }


    override fun merge(other: ConfigElement?) {
        when (other) {
            is ConfigArray -> {
                val allKeys = contents.keys + other.contents.keys
                allKeys.forEach { key ->
                    val currentValue = contents[key]
                    val otherValue = other.contents[key]

                    if(currentValue != null && otherValue != null) {
                        currentValue.merge(otherValue)
                    } else if (currentValue != null) {
                        return@forEach
                    } else if (otherValue != null) {
                        contents[key] = otherValue
                    }
                }
            }
            null -> return
            else -> throw IllegalStateException("Cannot merge with unlike type")
        }
    }

    private fun handleNullSet(rest: String, index: Int, value: String?) {
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
    require(index >= 0) { "Array node must not index negative"}
}
