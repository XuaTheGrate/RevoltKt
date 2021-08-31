package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Badges
import me.maya.revolt.api.Image
import me.maya.revolt.api.Relation
import me.maya.revolt.api.User

class UserImpl internal constructor(data: JsonObject, internal val state: State): User {
    override val id = data["_id"].string
    override val avatar = TODO() //data["avatar"].maybe { Image(it.jsonObject, state) }
    override val badges = Badges(data["badges"].int)
    override val online = data["online"].boolean
    override val relationship = Relation.valueOf(data["relationship"].string)
    override val status = data["status"].maybe {
        val obj = it.jsonObject
        User.Status(obj["text"].maybe { it.string }, User.Status.Presence.valueOf(obj["presence"].string))
    } ?: User.Status(null, User.Status.Presence.Online)
    override val username = data["username"].string

    internal val ownerId: String? = data["bot"].maybe { it.jsonObject["owner"].string }

    override val owner: User?
        get() = TODO("Not yet implemented")

    override fun update(data: User): User {
        TODO("Not yet implemented")
    }
}