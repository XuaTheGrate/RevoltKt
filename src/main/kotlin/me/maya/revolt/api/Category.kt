package me.maya.revolt.api

import me.maya.revolt.api.impl.CategoryImpl

interface Category: IChannelHolder, IHasID, IUpdateable<Category>{
    val title: String
    val server: Server

    fun update() = Update.fromExisting(this)

    data class Update(
        val id: String,
        val title: String,
        val channelIds: MutableList<String>
    ) {
        companion object {
            fun fromExisting(category: Category): Update {
                category as CategoryImpl
                return Update(category.id, category.title, category.channelIds.toMutableList())
            }
        }
    }
}