package me.maya.revolt.api

enum class ChannelType {
    Text, Voice;

    companion object {
        fun fromName(name: String) = when (name) {
            "TextChannel" -> Text
            "VoiceChannel" -> Voice
            else -> TODO()
        }
    }
}