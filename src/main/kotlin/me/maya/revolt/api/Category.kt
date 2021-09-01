package me.maya.revolt.api

interface Category: IChannelHolder, IHasID, IUpdateable<Category>{
    val title: String
    val server: Server
}