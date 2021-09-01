package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class MessageImpl internal constructor(
    data: JsonObject,
    val state: State
): Message {
    override var content: String = data["content"].string

    private val channelId = data["channel"].string
    private val authorId = data["author"].string

    private val _attachments = mutableListOf<Attachment>()

    override val channel: TextChannel get() = server.getChannel<TextChannel>(channelId)!!
    override val author: User get() = state.users.get(authorId)
    override val member: Member get() = server.getMember(authorId)!!

    override val attachments: List<Attachment> get() = _attachments.toList()
    override val server: Server get() = channel.server
    override val id: String
        get() = TODO("Not yet implemented")

    override fun update(data: Message): Message {
        TODO("Not yet implemented")
    }
}