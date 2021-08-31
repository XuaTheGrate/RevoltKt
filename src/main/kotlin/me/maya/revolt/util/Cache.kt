package me.maya.revolt.util

import me.maya.revolt.api.IHasID
import me.maya.revolt.api.IUpdateable

class Cache<T> where T: IHasID, T: IUpdateable<T> {
    private val mapping = mutableMapOf<String, T>()

    fun get(key: String): T? {
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
}