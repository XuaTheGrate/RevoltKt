package me.maya.revolt.api

interface Message: IHasID, IUpdateable<Message> {
    val channel: TextChannel
    val author: User
    val member: Member
    val content: String
    val attachments: List<Attachment>
    val server: Server
}