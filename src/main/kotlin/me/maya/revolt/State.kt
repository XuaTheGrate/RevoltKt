package me.maya.revolt

import com.mayak.json.JsonObject
import me.maya.revolt.api.*
import me.maya.revolt.api.impl.UserImpl
import me.maya.revolt.util.Cache
import kotlin.properties.Delegates

class State {
    var http by Delegates.notNull<HttpClient>()

    var cdn by Delegates.notNull<String>()
    var ws by Delegates.notNull<String>()
    val api: String = "https://api.revolt.chat"

    val users = Cache<User>()
    val categories = Cache<Category>()
    val servers = Cache<Server>()
    val members = Cache<Member>()
    val roles = Cache<Role>()
    val textChannels = Cache<TextChannel>()
    val voiceChannels = Cache<VoiceChannel>()

    suspend fun load() {
        val data = http.queryNode()
        cdn = data["features"].jsonObject["autumn"].jsonObject["url"].string
        ws = data["ws"].string + "?format=json"
    }

    fun readyCacheUpdate(data: JsonObject) {
        data["users"].jsonArray.forEach {
            val user = UserImpl(it.jsonObject, this)
            users.put(user.id, user)
        }


    }
}