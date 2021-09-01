package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Role
import me.maya.revolt.api.Server
import java.awt.Color

class RoleImpl internal constructor(
    override val id: String,
    private val serverId: String,
    data: JsonObject,
    val state: State
): Role {
    override val server: Server get() = state.servers.get(serverId)

    override var name = data["name"].string
    override var permissions: List<Int> = data["permissions"].jsonArray.map { it.int }
    override var colour: Color = Color.decode(data["colour"].string)
    override var hoist: Boolean = data["hoist"].boolean
    override var rank: Int = data["rank"].int

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