package me.maya.revolt.api

import me.maya.revolt.api.impl.ServerImpl
import me.maya.revolt.api.impl.TextChannelImpl
import me.maya.revolt.api.impl.VoiceChannelImpl
import java.io.File

interface Server: IHasID, IChannelHolder, IUpdateable<Server> {
    abstract class SystemMessages {
        abstract val userJoined: TextChannel?
        abstract val userLeft: TextChannel?
        abstract val userKicked: TextChannel?
        abstract val userBanned: TextChannel?

        internal abstract fun update(ujc: String?, ulc: String?, ukc: String?, ubc: String?)

        internal abstract fun update(other: SystemMessages)
    }

    data class Ban(val id: String, val reason: String?)

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

    suspend fun edit(
        name: String? = null,
        description: String? = null,
        // icon: Image? = null, // TODO
        // banner: Image? = null, // TODO
    )

    suspend fun editCategories(
        categories: List<Category.Update>
    )

    suspend fun editSystemMessages(
        userJoinedChannel: TextChannel? = null,
        userLeftChannel: TextChannel? = null,
        userKickedChannel: TextChannel? = null,
        userBannedChannel: TextChannel? = null
    )

    suspend fun leave()
    suspend fun delete() = leave()

    suspend fun createCategory(name: String)
    suspend fun createTextChannel(name: String, description: String? = null): TextChannel
    suspend fun createVoiceChannel(name: String, description: String? = null): VoiceChannel
    suspend fun createRole(name: String): Role.Partial

    suspend fun fetchMember(id: String): Member
    suspend fun fetchMembers(): List<Member>

    suspend fun kickMember(member: Member)

    suspend fun banMember(member: Member, reason: String? = null)
    suspend fun banMember(user: User, reason: String? = null)

    suspend fun unbanMember(member: Member)
    suspend fun unbanMember(user: User)

    suspend fun fetchBans(): List<Ban>
    suspend fun fetchInvites(): List<Invite>

    suspend fun setIcon(file: File)
    suspend fun setBanner(file: File)
}

inline fun <reified T: IChannel<T>> Server.getChannel(id: String): T? {
    this as ServerImpl
    val c = textChannelCache.maybeGet(id) ?: voiceChannelCache.maybeGet(id) ?: return null
    return c.takeIf { it is T } as T?
}

suspend inline fun <reified T: IChannel<T>> Server.fetchChannel(id: String): T {
    this as ServerImpl
    val data = state.http.getChannel(id)
    val ctype = ChannelType.fromName(data["type"].string)
    val expected = ChannelType.forType<T>()
    if (ctype != expected)
        throw IllegalArgumentException("channel has wrong type (expected $expected, got $ctype")
    return when (ctype) {
        ChannelType.Text -> TextChannelImpl(data, state)
        ChannelType.Voice -> VoiceChannelImpl(data, state)
    } as T
}