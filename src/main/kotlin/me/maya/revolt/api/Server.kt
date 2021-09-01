package me.maya.revolt.api

interface Server: IHasID, IChannelHolder, IUpdateable<Server> {
    abstract class SystemMessages {
        abstract val userJoined: TextChannel?
        abstract val userLeft: TextChannel?
        abstract val userKicked: TextChannel?
        abstract val userBanned: TextChannel?

        internal abstract fun update(ujc: String?, ulc: String?, ukc: String?, ubc: String?)

        internal abstract fun update(other: SystemMessages)
    }

    val name: String
    val description: String?
    val categories: List<Category>
    val roles: List<Role>
    val defaultPermissions: List<Int>
    val icon: Image?
    val banner: Image?
    val owner: Member
    val systemMessages: SystemMessages

    fun getCategory(id: String): Category?
    fun getMember(id: String): Member?
    fun getRole(id: String): Role?
}