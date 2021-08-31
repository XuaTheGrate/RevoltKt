package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class ServerImpl(
    data: JsonObject,
    val state: State
): Server {
    init {
        data["categories"].jsonArray.forEach {
            val cat = CategoryImpl(it.jsonObject, state)
            state.categories.put(cat.id, cat)
            categoryIds.add(cat.id)
        }

        data["roles"].jsonObject.forEach { id, r ->
            // val role =
        }
    }
    private val categoryIds = mutableListOf<String>()

    override val name = data["name"].string
    override val description: String? = data["description"].maybe { it.string }
    override val categories: List<Category>
        get() = TODO("Not yet implemented")

    override val roles: List<Role>
        get() = TODO("Not yet implemented")
    override val defaultPermissions: List<Int>
        get() = TODO("Not yet implemented")
    override val icon: Image?
        get() = TODO("Not yet implemented")
    override val banner: Image?
        get() = TODO("Not yet implemented")
    override val owner: Member
        get() = TODO("Not yet implemented")
    override val systemMessages: Server.SystemMessages
        get() = TODO("Not yet implemented")
    override val id: String
        get() = TODO("Not yet implemented")
    override val channels: List<IChannel<*>>
        get() = TODO("Not yet implemented")
    override val textChannels: List<TextChannel>
        get() = TODO("Not yet implemented")
    override val voiceChannels: List<VoiceChannel>
        get() = TODO("Not yet implemented")

    override fun update(data: Server): Server {
        TODO("Not yet implemented")
    }

}