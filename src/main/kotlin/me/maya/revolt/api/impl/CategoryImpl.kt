package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class CategoryImpl internal constructor(
    private val serverId: String,
    data: JsonObject,
    val state: State
): Category {
    override val id: String = data["id"].string
    override val server: Server get() = state.servers.get(serverId)

    override val textChannels: List<TextChannel>
        get() = server.textChannels.filter { it.id in channelIds }
    override val voiceChannels: List<VoiceChannel>
        get() = server.voiceChannels.filter { it.id in channelIds }

    override var title: String = data["title"].string

    private val channelIds = mutableSetOf<String>()

    override fun update(data: Category): Category {
        data as CategoryImpl

        title = data.title

        channelIds.clear()
        channelIds.addAll(data.channelIds)
        return this
    }

}