package me.maya.revolt.api

import com.mayak.json.Json
import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.impl.ServerImpl

class Invite internal constructor(
    data: JsonObject,
    internal val state: State
) {
    val id = data["_id"].string

    private val serverId = data["server"].string
    private val creatorId = data["creator"].string
    private val channelId = data["channel"].string

    val server: Server get() = state.servers.get(serverId)
    val creator: User get() = state.users.get(creatorId)
    val channel: IChannel<*> get() {
        val s = server as ServerImpl
        return s.textChannelCache.maybeGet(channelId) ?: s.voiceChannelCache.get(channelId)
    }

    suspend fun delete() {
        state.http.deleteInvite(id)
    }
}