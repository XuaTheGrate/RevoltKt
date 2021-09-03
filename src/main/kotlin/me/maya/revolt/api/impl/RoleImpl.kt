package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Role
import me.maya.revolt.api.Server
import me.maya.revolt.util.toPermissions
import java.awt.Color

class RoleImpl internal constructor(
    override val id: String,
    private val serverId: String,
    data: JsonObject,
    val state: State
): Role {
    override val server: Server get() = state.servers.get(serverId)

    override var name = data["name"].string
    override var permissions: Pair<Int, Int> = data["permissions"].jsonArray.toPermissions()
    override var colour: Color? = data["colour"].maybe { it.string }?.let { Color.decode(it) }
    override var hoist: Boolean = data["hoist"].maybe { it.boolean } ?: false
    override var rank: Int? = data["rank"].maybe { it.int }

    override suspend fun edit(name: String, hoist: Boolean?, rank: Int?, color: Color?, colour: Color?) {
        state.http.editRole(serverId, id, name, color ?: colour, hoist, rank)
    }

    override suspend fun delete() {
        state.http.deleteRole(serverId, id)
    }

    override fun update(data: Role): Role {
        data as RoleImpl
        name = data.name
        permissions = data.permissions
        colour = data.colour
        hoist = data.hoist
        rank = data.rank
        return this
    }
}