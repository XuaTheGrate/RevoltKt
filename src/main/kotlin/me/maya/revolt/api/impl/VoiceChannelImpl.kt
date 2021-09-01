package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.ChannelType
import me.maya.revolt.api.Image
import me.maya.revolt.api.Server
import me.maya.revolt.api.VoiceChannel

class VoiceChannelImpl internal constructor(
    data: JsonObject,
    val state: State
): VoiceChannel {
    private val serverId: String = data["server"].string

    override val type: ChannelType = ChannelType.Voice
    override val server: Server get() = state.servers.get(serverId)
    override val id: String = data["_id"].string

    override var name: String = data["name"].string
    override var description: String? = data["description"].maybe { it.string }
    override var icon: Image? = data["icon"].maybe {
        ImageImpl(it.jsonObject, state)
    }

    override fun update(data: VoiceChannel): VoiceChannel {
        data as VoiceChannelImpl

        name = data.name
        description = data.description
        icon = data.icon

        return this
    }
}