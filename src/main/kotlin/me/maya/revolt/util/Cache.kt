package me.maya.revolt.util

import me.maya.revolt.api.IHasID
import me.maya.revolt.api.IUpdateable

class Cache<T> internal constructor() where T: IHasID, T: IUpdateable<T> {
    val mapping = mutableMapOf<String, T>()

    fun get(key: String): T {
        return mapping[key]!!
    }

    fun maybeGet(key: String): T? {
        return mapping[key]
    }

    fun put(key: String, obj: T): T {
        if (key in mapping) {
            return mapping[key]!!.update(obj)
        }
        mapping[key] = obj
        return obj
    }

    fun drop(key: String): T? {
        return mapping.remove(key)
    }

    fun update(other: Cache<T>) {
        for ((k, v) in other.mapping) {
            put(k, v)
        }
    }

    fun replace(other: Cache<T>) {
        mapping.clear()
        update(other)
    }
}