package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*
import me.maya.revolt.util.restrictRange
import java.io.File

class TextChannelImpl @PublishedApi internal constructor(
    data: JsonObject,
    val state: State
): TextChannel {
    private val serverId = data["server"].string

    override val id: String = data["_id"].string
    override val type: ChannelType = ChannelType.Text
    override val server: Server get() = state.servers.get(serverId)

    override var lastMessage: String? = data["last_message"].maybe { it.string }
    override var name: String = data["name"].string
    override var description: String? = data["description"].maybe { it.string }
    override var icon: Image? = data["icon"].maybe {
        ImageImpl(it.jsonObject, state)
    }

    override suspend fun edit(name: String?, description: String?) {
        state.http.editChannel(id, name?.restrictRange(1..32), description?.restrictRange(0..1024))
    }

    override suspend fun sendMessage(content: String, replies: Map<Message, Boolean>): Message {
        val data = state.http.sendMessage(id, content, replies.takeIf { it.isNotEmpty() }?.map { (k, v) -> k.id to v }?.toMap())
        return MessageImpl(data, state)
    }

    override suspend fun fetchMessage(id: String): Message {
        val data = state.http.getMessage(this.id, id)
        return MessageImpl(data, state)
    }

    override suspend fun search(
        query: String,
        limit: Int,
        before: IHasID?,
        after: IHasID?,
        sort: SortOrder
    ): List<Message> {
        val messages = state.http.searchMessages(id, query, limit, before?.id, after?.id, sort)

        messages["users"].jsonArray.forEach {
            val user = UserImpl(it.jsonObject, state)
            state.users.put(user.id, user)
        }
        messages["members"].jsonArray.forEach {
            val member = MemberImpl(it.jsonObject, state)
            (server as ServerImpl).memberCache.put(member.id, member)
        }

        return messages["messages"].jsonArray.map { MessageImpl(it.jsonObject, state) }
    }

    override suspend fun delete() {
        state.http.deleteChannel(id)
    }

    private suspend fun setIcon(data: ByteArray, filename: String) {
        val imageId = state.http.uploadFile("icons", filename, data)
        state.http.editChannel(id, icon = imageId)
    }

    override suspend fun setIcon(file: File) {
        val data = file.readBytes()
        setIcon(data, file.name)
    }

    override suspend fun createInvite(): String {
        return state.http.createInvite(id)
    }

    override fun update(data: TextChannel): TextChannel {
        data as TextChannelImpl

        name = data.name
        description = data.description
        icon = data.icon

        return this
    }
}