package me.maya.revolt.api

interface Attachment: IHasID {
    val tag: String
    override val id: String
    val size: Int
    val filename: String
    val contentType: String

    fun getUrl(): String
}