package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class TextChannelImpl internal constructor(
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

    override fun update(data: TextChannel): TextChannel {
        data as TextChannelImpl

        name = data.name
        description = data.description
        icon = data.icon

        return this
    }
}