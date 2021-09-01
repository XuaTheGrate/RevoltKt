package me.maya.revolt.api

interface IChannelHolder {
    val textChannels: List<TextChannel>
    val voiceChannels: List<VoiceChannel>
}