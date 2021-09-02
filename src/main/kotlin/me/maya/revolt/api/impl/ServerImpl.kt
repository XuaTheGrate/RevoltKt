package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*
import me.maya.revolt.util.Cache

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
}

inline fun <reified T: IChannel<T>> Server.getChannel(id: String): T? {
    this as ServerImpl
    val c = textChannelCache.maybeGet(id) ?: voiceChannelCache.maybeGet(id) ?: return null
    return c.takeIf { it is T } as T?
}