package me.maya.revolt.api

import java.io.File

interface IChannel<T: IChannel<T>>: IHasID, IUpdateable<T> {
    val type: ChannelType
    val server: Server
    val name: String
    val description: String?
    val icon: Image?

    suspend fun edit(
        name: String? = null,
        description: String? = null,
        // icon: Image? = null // TODO
    )

    suspend fun delete()

    suspend fun setIcon(file: File)

    suspend fun createInvite(): String
}