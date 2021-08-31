package me.maya.revolt.api

interface IChannel<T: IChannel<T>>: IHasID, IUpdateable<T> {
    val type: ChannelType
    val server: Server
    val name: String
    val description: String?
    val icon: Image?
    val category: Category?
}