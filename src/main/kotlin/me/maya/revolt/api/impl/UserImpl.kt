package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import io.ktor.client.request.*
import io.ktor.client.statement.*
import me.maya.revolt.State
import me.maya.revolt.api.Badges
import me.maya.revolt.api.Relation
import me.maya.revolt.api.User
import me.maya.revolt.util.restrictRange
import java.io.File
import java.net.URI
import java.nio.file.Paths
import kotlin.text.get

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

    override suspend fun edit(status: User.Status?, profileContent: String?) {
        if (id != state.selfUserId) throw IllegalStateException("Cannot edit the profile of a user other than myself")

        if (status == null && profileContent == null) return

        state.http.editSelf(status, profileContent?.restrictRange(1..128))
    }

    private suspend fun setAvatar(imageData: ByteArray, filename: String) {
        val imageId = state.http.uploadFile("avatars", filename, imageData)
        state.http.editSelf(avatar = imageId)
    }

    override suspend fun setAvatar(file: File) {
        if (id != state.selfUserId) throw IllegalArgumentException("Cannot edit the profile of a user other than myself")

        val imageData = file.readBytes()
        setAvatar(imageData, file.name)
    }

    private suspend fun setBanner(imageData: ByteArray, filename: String) {
        val imageId = state.http.uploadFile("banner", filename, imageData)
        state.http.editSelf(profileBackground = imageId)
    }

    override suspend fun setBanner(file: File) {
        if (id != state.selfUserId) throw IllegalArgumentException("Cannot edit the profile of a user other than myself")

        val imageData = file.readBytes()
        setBanner(imageData, file.name)
    }

    override suspend fun fetchProfile(): User.Profile {
        val data = state.http.getProfile(id)
        val img = data["background"].maybe { ImageImpl(it.jsonObject, state) }
        return User.Profile(data["content"].maybe { it.string }, img)
    }

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