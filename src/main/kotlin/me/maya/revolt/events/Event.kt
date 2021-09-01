package me.maya.revolt.events

import me.maya.revolt.api.Message as IMessage

sealed class Event {
    object Ready: Event()

    class Message(val message: IMessage): Event()
}
