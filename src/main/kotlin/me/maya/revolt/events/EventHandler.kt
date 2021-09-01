package me.maya.revolt.events

open class EventHandler {
    open suspend fun onEvent(event: Event) {}

    open suspend fun onMessage(event: Event.Message) {}
}