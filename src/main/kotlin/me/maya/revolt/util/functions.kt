package me.maya.revolt.util

import com.mayak.json.JsonArray
import java.awt.Color

fun <T> Collection<T?>.isNulls(): Boolean = all { it == null }

fun Color.toHexString(): String = StringBuilder("#")
    .append(red.toString(16).padStart(2, '0'))
    .append(green.toString(16).padStart(2, '0'))
    .append(blue.toString(16).padStart(2, '0'))
    .toString()

fun String.restrictRange(range: IntRange): String {
    if (length !in range) throw IllegalArgumentException("string length not in range $range")
    return this
}

fun Int.restrictRange(range: IntRange): Int {
    if (this !in range) throw IllegalArgumentException("argument not in range $range")
    return this
}

fun JsonArray.toPermissions(): Pair<Int, Int> = first().int to last().int
