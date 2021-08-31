package me.maya.revolt.api

interface TextChannel: IChannel<TextChannel> {
    val lastMessage: String // TODO
}