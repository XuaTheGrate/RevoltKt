package me.maya.revolt.api.impl

import com.mayak.json.JsonObject
import me.maya.revolt.State
import me.maya.revolt.api.Role
import java.awt.Color

class RoleImpl(
    override val id: String,
    data: JsonObject,
    val state: State
): Role {
    override var name = data["name"].string
    override var permissions: List<Int> = data["permissions"].jsonArray.map { it.int }
    override var colour: Color = Color.decode(data["color"].string.trimStart('#'))
    override var hoist: Boolean = data["hoist"].boolean
    override var rank: Int = data["rank"].int

    override fun update(data: Role): Role {
        data as RoleImpl

        return this
    }

}