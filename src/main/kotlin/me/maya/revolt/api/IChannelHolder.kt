package me.maya.revolt.api

interface IChannelHolder {
    val channels: List<IChannel<*>>
    val textChannels: List<TextChannel>
    val voiceChannels: List<VoiceChannel>
}