package me.maya.revolt

import com.mayak.json.Json
import com.mayak.json.JsonArray
import com.mayak.json.JsonNode
import com.mayak.json.JsonObject
import io.azam.ulidj.ULID
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.maya.revolt.api.*
import me.maya.revolt.errors.RevoltError
import me.maya.revolt.util.isNulls
import me.maya.revolt.util.toHexString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.net.URLEncoder
import java.nio.file.Files
import kotlin.properties.Delegates
import kotlin.time.Duration

class HttpClient internal constructor(val state: State) {
    init {
        state.http = this
    }

    val logger: Logger = LoggerFactory.getLogger(HttpClient::class.java)

    companion object {
        fun newState() = HttpClient(State())
    }

    var token: String by Delegates.notNull()

    val client = HttpClient(CIO) {
        expectSuccess = false
        install(WebSockets) {
            pingInterval = Duration.seconds(30).inWholeMilliseconds
        }
    }

    fun HttpResponse.raiseForStatus() {
        when (status.value) {
            in 200..399 -> return

            400 -> throw RevoltError.Api.BadRequest()
            401 -> throw RevoltError.Api.Unauthorized()
            403 -> throw RevoltError.Api.Forbidden()
            404 -> throw RevoltError.Api.NotFound()

            in 500..599 -> throw RevoltError.Api.InternalError(status.value)

            else -> throw IllegalStateException("unexpected error code $status")
        }
    }

    suspend fun request(method: HttpMethod, route: String, params: Map<String, String>? = null, body: JsonNode? = null): JsonNode? {
        val data = client.request<HttpResponse> {
            this.method = method

            header("x-bot-token", token)

            var urlWithParams = StringBuilder(state.api + route)

            if (params != null) {
                urlWithParams.append("?")
                params.forEach { (k, v) ->
                    urlWithParams.append(URLEncoder.encode(k, "UTF-8"))
                    urlWithParams.append("=")
                    urlWithParams.append(URLEncoder.encode(v, "UTF-8"))
                }
            }
            url(urlWithParams.toString())

            if (body != null) {
                this.body = Json.stringify(body)
            }
        }
        logger.info("${method.value} \"$route\": ${data.status}")
        data.raiseForStatus()

        val read = data.readText(Charsets.UTF_8)
        return read.takeIf { it.isNotBlank() }?.let { Json.parse(it) }
    }

    suspend fun queryNode() = request(HttpMethod.Get, "/")!!.jsonObject

    suspend fun getUser(userId: String): JsonObject {
        return request(HttpMethod.Get, "/users/$userId")!!.jsonObject
    }

    suspend fun editSelf(
        status: User.Status? = null,
        avatar: String? = null,
        profileContent: String? = null,
        profileBackground: String? = null
    ) {
        request(HttpMethod.Patch, "/users/@me", body = Json.createObject {
            if (status != null) set("status", Json.createObject {
                status.text?.let { set("text", it) }
                set("presence", status.presence.name)
            })

            if (avatar != null) set("avatar", avatar)

            val profile = mutableMapOf<String, String>()
            if (profileContent != null) profile["content"] = profileContent
            if (profileBackground != null) profile["background"] = profileBackground

            if (profile.isNotEmpty()) set("profile", profile)
        })
    }

    suspend fun getProfile(userId: String): JsonObject {
        return request(HttpMethod.Get, "/users/$userId/profile")!!.jsonObject
    }

    suspend fun getChannel(channelId: String): JsonObject {
        return request(HttpMethod.Get, "/channels/$channelId")!!.jsonObject
    }

    suspend fun editChannel(
        channelId: String,
        name: String? = null,
        description: String? = null,
        icon: String? = null
    ) {
        if (name == null && description == null) return
        request(HttpMethod.Patch, "/channels/$channelId", body = Json.createObject {
            if (name != null) set("name", name)
            if (description != null) set("description", description)
            if (icon != null) set("icon", icon)
        })
    }

    suspend fun deleteChannel(channelId: String) {
        request(HttpMethod.Delete, "/channels/$channelId")
    }

    suspend fun createInvite(channelId: String): String {
        val data = request(HttpMethod.Post, "/channels/$channelId/invites")
        return data!!.jsonObject["code"].string
    }

    suspend fun sendMessage(
        channelId: String,
        content: String,
        // attachments: List<String>? = null, // TODO
        replies: Map<String, Boolean>? = null
    ): JsonObject {
        val data = request(HttpMethod.Post, "/channels/$channelId/messages", body = Json.createObject {
            set("content", content)
            if (replies != null) set("replies", Json.createObject(replies))
        })
        return data!!.jsonObject
    }

    suspend fun getMessageHistory(
        channelId: String,
        sortOrder: SortOrder = SortOrder.Latest,
        before: String? = null,
        after: String? = null
    ): JsonObject = TODO("paginated message history with flows")

    suspend fun getMessageHistory(
        channelId: String,
        sortOrder: SortOrder = SortOrder.Latest,
        nearby: String
    ): JsonObject = TODO("paginated message history with flows")

    suspend fun getMessage(channelId: String, messageId: String): JsonObject {
        return request(HttpMethod.Get, "/channels/$channelId/messages/$messageId")!!.jsonObject
    }

    suspend fun editMessage(channelId: String, messageId: String, content: String) {
        request(HttpMethod.Patch, "/channels/$channelId/messages/$messageId", body = Json.createObject { set("content", content) })
    }

    suspend fun deleteMessage(channelId: String, messageId: String) {
        request(HttpMethod.Delete, "/channels/$channelId/messages/$messageId")
    }

    suspend fun searchMessages(
        channelId: String,
        query: String,
        limit: Int = 100,
        before: String? = null,
        after: String? = null,
        sort: SortOrder = SortOrder.Relevance
    ): JsonObject {
        return request(HttpMethod.Post, "/channels/$channelId/search", body = Json.createObject {
            set("query", query)
            set("limit", limit)
            if (before != null) set("before", before)
            if (after != null) set("after", after)
            set("sort", sort.name)
            set("include_users", true)
        })!!.jsonObject
    }

    suspend fun getServer(serverId: String): JsonObject {
        return request(HttpMethod.Get, "/servers/$serverId")!!.jsonObject
    }

    suspend fun editServer(
        serverId: String,
        name: String? = null,
        description: String? = null,
        icon: String? = null,
        banner: String? = null,
        categories: List<Category.Update>? = null,
        systemMessages: Server.SystemMessages? = null
    ) {
        if (listOf(name, description, categories, systemMessages).isNulls()) return
        val body = Json.createObject {
            if (name != null) set("name", name)
            if (description != null) set("description", description)

            if (icon != null) set("icon", icon)
            if (banner != null) set("banner", banner)

            if (categories != null) {
                set("categories", categories.map { c ->
                    Json.createObject {
                        set("id", c.id)
                        set("title", c.title)
                        set("channels", c.channelIds)
                    }
                })
            }

            if (systemMessages != null) {
                set("system_messages", Json.createObject {
                    if (systemMessages.userJoined != null) set("user_joined", systemMessages.userJoined!!.id)
                    if (systemMessages.userLeft != null) set("user_left", systemMessages.userLeft!!.id)
                    if (systemMessages.userKicked != null) set("user_kicked", systemMessages.userKicked!!.id)
                    if (systemMessages.userBanned != null) set("user_banned", systemMessages.userBanned!!.id)
                })
            }
        }
        request(HttpMethod.Patch, "/servers/$serverId", body = body)
    }

    suspend fun leaveServer(serverId: String) {
        request(HttpMethod.Delete, "/servers/$serverId")
    }

    suspend fun createChannel(
        serverId: String,
        type: ChannelType,
        name: String,
        description: String? = null
    ): JsonObject {
        return request(HttpMethod.Post, "/servers/$serverId/channels", body = Json.createObject {
            set("type", type.name)
            set("name", name)
            set("nonce", ULID.random())
            if (description != null) set("description", description)
        })!!.jsonObject
    }

    suspend fun getInvites(serverId: String): JsonArray {
        return request(HttpMethod.Get, "/servers/$serverId/invites")!!.jsonArray
    }

    suspend fun getMember(serverId: String, memberId: String): JsonObject {
        return request(HttpMethod.Get, "/servers/$serverId/members/$memberId")!!.jsonObject
    }

    suspend fun editMember(
        serverId: String,
        memberId: String,
        nickname: String? = null,
        // avatar: Image? = null, // TODO
        roles: List<String>? = null
    ) {
        if (nickname == null && roles == null) return
        request(HttpMethod.Patch, "/servers/$serverId/members/$memberId", body = Json.createObject {
            if (nickname != null) set("nickname", nickname)
            if (roles != null) set("roles", roles)
        })
    }

    suspend fun kickMember(serverId: String, memberId: String) {
        request(HttpMethod.Delete, "/servers/$serverId/members/$memberId")
    }

    suspend fun getMembers(serverId: String): JsonObject {
        return request(HttpMethod.Get, "/servers/$serverId/members")!!.jsonObject
    }

    suspend fun banUser(serverId: String, userId: String, reason: String? = null) {
        request(HttpMethod.Put, "/servers/$serverId/bans/$userId", body = reason?.let { Json.createObject(mapOf("reason" to it)) })
    }

    suspend fun unbanUser(serverId: String, userId: String) {
        request(HttpMethod.Delete, "/servers/$serverId/bans/$userId")
    }

    suspend fun getBans(serverId: String): JsonObject {
        return request(HttpMethod.Get, "/servers/$serverId/bans")!!.jsonObject
    }

    /*
    suspend fun setRolePermission
    // TODO: these
    suspend fun setDefaultPermission
     */

    suspend fun createRole(serverId: String, name: String): JsonObject {
        return request(HttpMethod.Post, "/servers/$serverId/roles", body = Json.createObject { set("name", name) })!!.jsonObject
    }

    suspend fun editRole(
        serverId: String,
        roleId: String,
        name: String,
        color: Color? = null,
        hoist: Boolean? = null,
        rank: Int? = null
    ) {
        request(HttpMethod.Patch, "/servers/$serverId/roles/$roleId", body = Json.createObject {
            set("name", name)
            if (color != null) set("color", color.toHexString())
            if (hoist != null) set("hoist", hoist)
            if (rank != null) set("rank", rank)
        })
    }

    suspend fun deleteRole(serverId: String, roleId: String) {
        request(HttpMethod.Delete, "/servers/$serverId/roles/$roleId")
    }

    suspend fun getBotInfo(botId: String): JsonObject {
        return request(HttpMethod.Get, "/bots/$botId/invite")!!.jsonObject
    }

    suspend fun getInvite(inviteId: String): JsonObject = TODO(
            "ask the return info from this to be normalized because wtf is this shit"+
                   "https://developers.revolt.chat/api/#tag/Invites/paths/~1invites~1:invite/get")

    suspend fun deleteInvite(inviteId: String) {
        request(HttpMethod.Delete, "/invites/$inviteId")
    }

    private fun getImageMimeType(imageData: ByteArray): String {
        return URLConnection.guessContentTypeFromStream(ByteArrayInputStream(imageData))
    }

    suspend fun uploadFile(tag: String, filename: String, imageData: ByteArray): String {
        val contentType = getImageMimeType(imageData)
        val headers = headersOf(
            "Content-Type" to listOf(contentType),
            "Content-Disposition" to listOf("filename=$filename")
        )
        val formData = formData {
            append("file", imageData, headers)
        }

        val data = client.submitFormWithBinaryData<HttpResponse>(
            url = "${state.cdn}/$tag",
            formData = formData
        )
        data.raiseForStatus()
        return Json.parse(data.readText(Charsets.UTF_8)).jsonObject["id"].string
    }
}