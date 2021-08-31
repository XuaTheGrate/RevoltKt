package me.maya.revolt.api

import me.maya.revolt.util.BitFlags

class Badges(value: Int = 0): BitFlags(value) {
    var developer by Delegate(0)
    var translator by Delegate(1)
    var supporter by Delegate(2)
    var responsibleDisclosure by Delegate(3) // idk what this means lol
    var staff by Delegate(4)
    var earlyAdopter by Delegate(8)
}