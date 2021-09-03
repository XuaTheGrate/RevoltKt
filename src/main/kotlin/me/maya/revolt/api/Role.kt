package me.maya.revolt.api

import java.awt.Color

interface Role: IHasID, IUpdateable<Role> {
    val name: String
    val permissions: Pair<Int, Int> // TODO
    val colour: Color?
    val hoist: Boolean
    val rank: Int?
    val server: Server

    val color: Color?
        get() = colour

    suspend fun edit(
        name: String = this.name,
        hoist: Boolean? = null,
        rank: Int? = null,
        color: Color? = null,
        colour: Color? = null,
    )

    suspend fun delete()

    data class Partial(val id: String, val permissions: Pair<Int, Int>)
}