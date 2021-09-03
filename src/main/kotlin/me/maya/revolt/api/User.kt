package me.maya.revolt.api

import java.io.File

interface User: IHasID, IUpdateable<User> {
    data class Status(val text: String?, val presence: Presence) {
        enum class Presence {
            Online, Idle, Busy, Invisible
        }
    }

    data class Profile(val content: String?, val background: Image?)

    val username: String
    val avatar: Image?
    val status: Status
    val badges: Badges
    val online: Boolean
    val relationship: Relation

    val owner: User?

    suspend fun edit(
        status: Status? = null,
        profileContent: String? = null
    )

    suspend fun setAvatar(file: File)

    suspend fun setBanner(file: File)

    suspend fun fetchProfile(): Profile
}