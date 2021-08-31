package me.maya.revolt.api

import kotlin.reflect.KClass

sealed class Event {
    companion object {
        fun fromName(name: String): KClass<out Event> = Event::class.sealedSubclasses.first { it.simpleName == name }
    }

    object Pong: Event()
    object Ready: Event()
}
