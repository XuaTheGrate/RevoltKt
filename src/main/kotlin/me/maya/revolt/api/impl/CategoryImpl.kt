package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class CategoryImpl(
    data: JsonObject,
    val state: State
): Category {
    override var title: String = data["title"].string
    override val id: String = data["_id"].string

    private var channelIds = mutableSetOf<String>()

    override val channels: List<IChannel<*>>
        get() = channelIds.mapNotNull { state.textChannels.get(it) ?: state.voiceChannels.get(it) }

    override val textChannels: List<TextChannel>
        get() = channels.filterIsInstance<TextChannel>()
    override val voiceChannels: List<VoiceChannel>
        get() = channels.filterIsInstance<VoiceChannel>()

    override fun update(data: Category): Category {
        data as CategoryImpl

        title = data.title
        channelIds = data.channelIds
        return this
    }

}