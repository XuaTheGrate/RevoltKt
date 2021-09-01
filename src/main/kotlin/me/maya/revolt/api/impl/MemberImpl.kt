package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*

class MemberImpl internal constructor(
    data: JsonObject,
    val state: State
): Member {
    private val serverId: String
    private val roleIds = data["roles"].maybe { it.jsonArray.map { it.string }.toMutableSet() } ?: mutableSetOf()

    override val id: String

    init {
        val ids = data["_id"].jsonObject
        serverId = ids["server"].string
        id = ids["user"].string
    }
    override var nickname: String? = data["nickname"].maybe { it.string }
    override var avatar: Image? = data["avatar"].maybe {
        ImageImpl(it.jsonObject, state)
    }

    override val user: User get() = state.users.get(id)
    override val roles: List<Role> get() = roleIds.map { server.getRole(it)!! }
    override val server: Server get() = state.servers.get(serverId)

    override fun update(data: Member): Member {
        data as MemberImpl

        nickname = data.nickname
        avatar = data.avatar

        roleIds.clear()
        roleIds.addAll(data.roleIds)

        return this
    }

}