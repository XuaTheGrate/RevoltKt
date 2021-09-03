package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.*
import me.maya.revolt.util.restrictRange

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

    override suspend fun edit(nickname: String?) {
        state.http.editMember(serverId, id, nickname?.restrictRange(1..32))
    }

    override suspend fun addRole(role: Role) {
        val updated = roles.map { it.id }.toMutableSet()
        if (!updated.add(role.id)) return
        state.http.editMember(serverId, id, roles = updated.toList())
    }

    override suspend fun addRoles(vararg roles: Role) {
        val updated = this.roles.map { it.id }.toMutableSet()
        if (!updated.addAll(roles.map { it.id })) return
        state.http.editMember(serverId, id, roles = updated.toList())
    }

    override suspend fun removeRole(role: Role) {
        val updated = roles.map { it.id }.toMutableSet()
        if (!updated.remove(role.id)) return
        state.http.editMember(serverId, id, roles = updated.toList())
    }

    override suspend fun removeRoles(vararg roles: Role) {
        val updated = this.roles.map { it.id }.toMutableSet()
        if (!updated.removeAll(roles.map { it.id })) return
        state.http.editMember(serverId, id, roles = updated.toList())
    }

    override suspend fun updateRoles(roles: List<Role>) {
        state.http.editMember(serverId, id, roles = roles.map { it.id })
    }

    override suspend fun kick() {
        server.kickMember(this)
    }

    override suspend fun ban(reason: String?) {
        server.banMember(this, reason)
    }

    override suspend fun unban() {
        server.unbanMember(this)
    }

    override fun update(data: Member): Member {
        data as MemberImpl

        nickname = data.nickname
        avatar = data.avatar

        roleIds.clear()
        roleIds.addAll(data.roleIds)

        return this
    }

}