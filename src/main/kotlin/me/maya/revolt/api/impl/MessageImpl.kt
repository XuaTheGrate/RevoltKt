package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*
import me.maya.revolt.util.restrictRange

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

    override val id: String = data["_id"].string

    override suspend fun edit(content: String) {
        state.http.editMessage(channelId, id, content.restrictRange(1..2000))
    }

    override suspend fun delete() {
        state.http.deleteMessage(channelId, id)
    }

    override suspend fun reply(content: String, mention: Boolean): Message {
        val data = state.http.sendMessage(channelId, content, replies = mapOf(id to mention))
        return MessageImpl(data, state)
    }

    override fun update(data: Message): Message {
        data as MessageImpl

        content = data.content
        _attachments.clear()
        _attachments.addAll(data._attachments)

        return this
    }
}