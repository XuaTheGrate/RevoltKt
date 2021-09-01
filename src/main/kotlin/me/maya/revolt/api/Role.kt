package me.maya.revolt.api

import java.awt.Color

interface Role: IHasID, IUpdateable<Role> {
    val name: String
    val permissions: List<Int> // ???
    val colour: Color
    val hoist: Boolean
    val rank: Int
    val server: Server

    val color: Color
        get() = colour
}