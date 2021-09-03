package me.maya.revolt.api

interface Member: IHasID, IUpdateable<Member> {
    val nickname: String?
    val user: User
    val roles: List<Role>
    val avatar: Image?
    val server: Server

    suspend fun edit(
        nickname: String? = null,
        // avatar: Image? = null, // TODO
    )

    suspend fun addRole(role: Role)
    suspend fun addRoles(vararg roles: Role)

    suspend fun removeRole(role: Role)
    suspend fun removeRoles(vararg roles: Role)

    suspend fun updateRoles(roles: List<Role>)

    suspend fun kick()
    suspend fun ban(reason: String? = null)
    suspend fun unban()
}