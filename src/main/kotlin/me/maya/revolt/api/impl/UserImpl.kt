package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Badges
import me.maya.revolt.api.Image
import me.maya.revolt.api.Relation
import me.maya.revolt.api.User

class UserImpl internal constructor(
    data: JsonObject,
    val state: State
): User {
    override val id = data["_id"].string

    override var avatar = data["avatar"].maybe { ImageImpl(it.jsonObject, state) }
    override var badges = Badges(data["badges"].int)
    override var online = data["online"].boolean
    override var relationship = Relation.valueOf(data["relationship"].string)
    override var username = data["username"].string

    override var status = data["status"].maybe {
        val obj = it.jsonObject
        User.Status(obj["text"].maybe { it.string }, User.Status.Presence.valueOf(obj["presence"].string))
    } ?: User.Status(null, User.Status.Presence.Online)

    internal val ownerId: String? = data["bot"].maybe { it.jsonObject["owner"].string }

    override val owner: User? get() = ownerId?.let { state.users.get(it) }

    override fun update(data: User): User {
        data as UserImpl

        avatar = data.avatar
        badges = data.badges
        online = data.online
        relationship = data.relationship
        username = data.username
        status = data.status

        return this
    }
}