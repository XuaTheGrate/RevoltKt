package me.maya.revolt.util

import kotlin.reflect.KProperty

open class BitFlags(var value: Int) {
    class Delegate(flag: Int) {
        private val flag = 1 shl flag

        operator fun getValue(thisRef: BitFlags, property: KProperty<*>): Boolean {
            return thisRef.value and flag != 0
        }

        operator fun setValue(thisRef: BitFlags, property: KProperty<*>, value: Boolean) {
            if (value) {
                thisRef.value = thisRef.value or flag
            } else {
                thisRef.value = thisRef.value and flag.inv()
            }
        }
    }


}