package me.maya.revolt

import com.mayak.json.JsonObject
import me.maya.revolt.api.*
import me.maya.revolt.api.impl.*
import me.maya.revolt.util.Cache
import kotlin.properties.Delegates

class State {
    var http by Delegates.notNull<HttpClient>()

    var cdn by Delegates.notNull<String>()
    var ws by Delegates.notNull<String>()
    val api: String = "https://api.revolt.chat"
    var selfUserId: String by Delegates.notNull()

    val users = Cache<User>()
    val servers = Cache<Server>()

    suspend fun load() {
        val data = http.queryNode()
        cdn = data["features"].jsonObject["autumn"].jsonObject["url"].string
        ws = data["ws"].string + "?format=json"
    }

    suspend fun readyCacheUpdate(data: JsonObject) {
        // this assertion ensures future changes where user objects may be included
        assert(data["users"].jsonArray.size == 1)

        val userData = data["users"].jsonArray.first()
        val user = UserImpl(userData.jsonObject, this)
        users.put(user.id, user)

        selfUserId = user.id

        data["servers"].jsonArray.forEach {
            val server = ServerImpl(it.jsonObject, this)

            val memberData = http.getMembers(server.id)
            memberData["users"].jsonArray.forEach {
                val user = UserImpl(it.jsonObject, this)
                users.put(user.id, user)
            }
            memberData["members"].jsonArray.forEach {
                val member = MemberImpl(it.jsonObject, this)
                server.memberCache.put(member.id, member)
            }

            servers.put(server.id, server)
        }
        data["channels"].jsonArray.forEach { val it = it.jsonObject
            val serverId = it["server"].maybe { it.string } ?: return@forEach
            val server = servers.get(serverId) as ServerImpl

            when (val type = it["channel_type"].string) {
                "TextChannel" -> TextChannelImpl(it, this).apply { server.textChannelCache.put(id, this) }
                "VoiceChannel" -> VoiceChannelImpl(it, this).apply { server.voiceChannelCache.put(id, this) }
                else -> throw IllegalArgumentException("unknown channel type $type")
            }
        }
        data["members"].jsonArray.forEach { val it = it.jsonObject
            val member = MemberImpl(it, this)
            (member.server as ServerImpl).memberCache.put(member.id, member)
        }
    }
}