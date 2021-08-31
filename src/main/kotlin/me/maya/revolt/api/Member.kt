package me.maya.revolt.api

interface Member: IHasID, IUpdateable<Member> {
    val nickname: String
    val user: User
}