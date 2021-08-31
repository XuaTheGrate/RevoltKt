package me.maya.revolt.api

interface Image: Attachment {
    val width: Int
    val height: Int

    fun getUrl(maxSize: Int = 0): String
}