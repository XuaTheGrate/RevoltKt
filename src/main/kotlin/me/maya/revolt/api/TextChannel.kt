package me.maya.revolt.api

import kotlinx.coroutines.flow.Flow

interface TextChannel: IChannel<TextChannel> {
    val lastMessage: String? // TODO

    suspend fun sendMessage(
        content: String,
        replies: Map<Message, Boolean> = emptyMap()
    ): Message

    suspend fun history(): Flow<Message> = TODO("paginated message history")

    suspend fun fetchMessage(id: String): Message

    suspend fun search(
        query: String,
        limit: Int = 100,
        before: IHasID? = null,
        after: IHasID? = null,
        sort: SortOrder = SortOrder.Relevance
    ): List<Message>
}