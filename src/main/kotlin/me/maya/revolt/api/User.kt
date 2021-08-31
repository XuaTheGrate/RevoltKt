package me.maya.revolt.api

interface User: IHasID, IUpdateable<User> {
    class Status(val text: String?, val presence: Presence) {
        enum class Presence {
            Online, Idle, Busy, Invisible
        }
    }

    val username: String
    val avatar: Image?
    val status: Status
    val badges: Badges
    val online: Boolean
    val relationship: Relation

    val owner: User?
}