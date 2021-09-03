package me.maya.revolt.api

import kotlin.reflect.KClass

enum class ChannelType {
    Text, Voice;

    companion object {
        fun fromName(name: String) = when (name) {
            "Text", "TextChannel" -> Text
            "Voice", "VoiceChannel" -> Voice
            else -> TODO()
        }

        inline fun <reified T: IChannel<T>> forType() = forType(T::class)

        @PublishedApi
        internal fun <T: IChannel<T>> forType(cls: KClass<out T>) = when (cls) {
            TextChannel::class -> Text
            VoiceChannel::class -> Voice
            else -> throw IllegalArgumentException("unknown channel type")
        }
    }
}