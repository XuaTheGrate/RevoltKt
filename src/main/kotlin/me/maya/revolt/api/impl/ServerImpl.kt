package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import io.azam.ulidj.ULID
import me.maya.revolt.State
import me.maya.revolt.api.*
import me.maya.revolt.util.Cache
import me.maya.revolt.util.restrictRange
import me.maya.revolt.util.toPermissions
import java.io.File

class ServerImpl internal constructor(
    data: JsonObject,
    val state: State
): Server {
    override val systemMessages: Server.SystemMessages = object: Server.SystemMessages() {
        private var userJoinedChannel: String? = null
        private var userLeftChannel: String? = null
        private var userKickedChannel: String? = null
        private var userBannedChannel: String? = null

        init {
            val d = data["system_messages"].jsonObject
            update(
                d["user_joined"].maybe { it.string },
                d["user_left"].maybe { it.string },
                d["user_kicked"].maybe { it.string },
                d["user_banned"].maybe { it.string }
            )
        }

        override fun update(ujc: String?, ulc: String?, ukc: String?, ubc: String?) {
            userJoinedChannel = ujc
            userLeftChannel = ulc
            userKickedChannel = ukc
            userBannedChannel = ubc
        }

        override fun update(other: Server.SystemMessages) {
            update(other.userJoined?.id, other.userLeft?.id, other.userKicked?.id, other.userBanned?.id)
        }

        override val userJoined: TextChannel? get() = userJoinedChannel?.let { textChannelCache.get(it) }
        override val userLeft: TextChannel? get() = userLeftChannel?.let { textChannelCache.get(it) }
        override val userKicked: TextChannel? get() = userKickedChannel?.let { textChannelCache.get(it) }
        override val userBanned: TextChannel? get() = userBannedChannel?.let { textChannelCache.get(it) }
    }

    private val roleCache = Cache<Role>()
    private val categoryCache = Cache<Category>()
    internal val memberCache = Cache<Member>()
    @PublishedApi internal val textChannelCache = Cache<TextChannel>()
    @PublishedApi internal val voiceChannelCache = Cache<VoiceChannel>()

    private var ownerId = data["owner"].string
    override val owner: Member get() = memberCache.get(ownerId)

    override var name = data["name"].string
    override var description: String? = data["description"].maybe { it.string }

    override val categories: List<Category> get() = categoryCache.mapping.values.toList()
    override val roles: List<Role> get() = roleCache.mapping.values.toList()

    override val defaultPermissions: List<Int> get() = TODO("Permissions are undocumented, and a bit confusing")

    override var icon: Image? = data["icon"].maybe {
        ImageImpl(it.jsonObject, state)
    }
    override var banner: Image? = data["banner"].maybe {
        ImageImpl(it.jsonObject, state)
    }
    override val id: String = data["_id"].string

    override val textChannels: List<TextChannel> get() = textChannelCache.mapping.values.toList()
    override val voiceChannels: List<VoiceChannel> get() = voiceChannelCache.mapping.values.toList()

    init {
        data["categories"].maybe {
            it.jsonArray.forEach {
                val cat = CategoryImpl(id, it.jsonObject, state)
                categoryCache.put(cat.id, cat)
            }
        }

        data["roles"].maybe {
            it.jsonObject.forEach { id, r ->
                val role = RoleImpl(id, this.id, r.jsonObject, state)
                roleCache.put(id, role)
            }
        }
    }

    override fun update(data: Server): Server {
        data as ServerImpl

        textChannelCache.replace(data.textChannelCache)
        voiceChannelCache.replace(data.voiceChannelCache)
        roleCache.replace(data.roleCache)
        memberCache.replace(data.memberCache)
        categoryCache.replace(data.categoryCache)
        roleCache.replace(data.roleCache)

        name = data.name
        icon = data.icon
        banner = data.banner
        description = data.description

        systemMessages.update(data.systemMessages)
        return this
    }

    override fun getCategory(id: String): Category? {
        return categoryCache.maybeGet(id)
    }

    override fun getMember(id: String): Member? {
        return memberCache.maybeGet(id)
    }

    override fun getRole(id: String): Role? {
        return roleCache.maybeGet(id)
    }

    override suspend fun edit(name: String?, description: String?) {
        state.http.editServer(id, name?.restrictRange(1..32), description?.restrictRange(0..1024))
    }

    override suspend fun editCategories(categories: List<Category.Update>) {
        state.http.editServer(id, categories = categories)
    }

    override suspend fun editSystemMessages(
        userJoinedChannel: TextChannel?,
        userLeftChannel: TextChannel?,
        userKickedChannel: TextChannel?,
        userBannedChannel: TextChannel?
    ) {
        state.http.editServer(id, systemMessages = object: Server.SystemMessages() {
            override val userJoined = userJoinedChannel
            override val userLeft = userLeftChannel
            override val userKicked = userKickedChannel
            override val userBanned = userBannedChannel

            override fun update(ujc: String?, ulc: String?, ukc: String?, ubc: String?) = throw RuntimeException()
            override fun update(other: Server.SystemMessages) = throw RuntimeException()
        })
    }

    override suspend fun leave() {
        state.http.leaveServer(id)
    }

    override suspend fun fetchInvites(): List<Invite> {
        val data = state.http.getInvites(id)
        return data.map { Invite(it.jsonObject, state) }
    }

    override suspend fun banMember(member: Member, reason: String?) = state.http.banUser(id, member.id, reason)
    override suspend fun banMember(user: User, reason: String?) = state.http.banUser(id, user.id, reason)

    override suspend fun unbanMember(member: Member) {
        state.http.unbanUser(id, member.id)
    }

    override suspend fun unbanMember(user: User) {
        state.http.unbanUser(id, user.id)
    }

    override suspend fun fetchBans(): List<Server.Ban> {
        val data = state.http.getBans(id)
        return data["bans"].jsonArray.map { val it = it.jsonObject
            Server.Ban(it["_id"].jsonObject["user"].string, it["reason"].maybe { it.string })
        }
    }

    override suspend fun createCategory(name: String) {
        state.http.editServer(id, categories = categories.map { it.update() } + listOf(Category.Update(ULID.random(), name, mutableListOf())))
    }

    override suspend fun createTextChannel(name: String, description: String?): TextChannel {
        val data = state.http.createChannel(id, ChannelType.Text, name.restrictRange(1..32), description?.restrictRange(0..1024))
        return TextChannelImpl(data, state)
    }

    override suspend fun createVoiceChannel(name: String, description: String?): VoiceChannel {
        val data = state.http.createChannel(id, ChannelType.Voice, name.restrictRange(1..32), description?.restrictRange(0..1024))
        return VoiceChannelImpl(data, state)
    }

    override suspend fun fetchMember(id: String): Member {
        val data = state.http.getMember(this.id, id)
        return MemberImpl(data, state)
    }

    override suspend fun fetchMembers(): List<Member> {
        val members = state.http.getMembers(id)
        return members["members"].jsonArray.map { MemberImpl(it.jsonObject, state) }
    }

    override suspend fun kickMember(member: Member) {
        state.http.kickMember(id, member.id)
    }

    override suspend fun createRole(name: String): Role.Partial {
        val data = state.http.createRole(id, name)
        return Role.Partial(data["id"].string, data["permissions"].jsonArray.toPermissions())
    }

    private suspend fun setImage(tag: String, data: ByteArray, filename: String) {
        val imageId = state.http.uploadFile(tag, filename, data)
        if (tag == "banner") state.http.editServer(id, banner = imageId)
        else state.http.editServer(id, icon = imageId)
    }

    override suspend fun setBanner(file: File) {
        val data = file.readBytes()
        setImage("banners", data, file.name)
    }

    override suspend fun setIcon(file: File) {
        val data = file.readBytes()
        setImage("icons", data, file.name)
    }
}