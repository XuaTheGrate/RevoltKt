package me.maya.revolt.api

interface Server: IHasID, IChannelHolder, IUpdateable<Server> {
    data class SystemMessages(
        val userJoined: TextChannel?,
        val userLeft: TextChannel?,
        val userKicked: TextChannel?,
        val userBanned: TextChannel?
    )

    val name: String
    val description: String?
    val categories: List<Category>
    val roles: List<Role>
    val defaultPermissions: List<Int>
    val icon: Image?
    val banner: Image?
    val owner: Member
    val systemMessages: SystemMessages
}