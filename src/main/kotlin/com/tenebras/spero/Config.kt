package com.tenebras.spero

import java.util.*

class Config(val items: Map<String, Any> = HashMap()) {

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T {
        if (!items.containsKey(key)) {
            throw ArrayIndexOutOfBoundsException("ConfigValue doesn't have key '$key'")
        }

        return items.get(key) as T
    }

    fun set(key: String, value: Any): Config {

        items.plus(Pair(key, value))
        return this
    }
}