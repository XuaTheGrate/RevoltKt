package me.maya.revolt

import com.mayak.json.Json
import com.mayak.json.JsonArray
import com.mayak.json.JsonNode
import com.mayak.json.JsonObject
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.maya.revolt.errors.RevoltError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLEncoder
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
        return Json.parse(read)
    }

    suspend fun queryNode(): JsonObject {
        val data = request(HttpMethod.Get, "/")
        return data!!.jsonObject
    }

    suspend fun getMembers(serverId: String): JsonObject {
        val data = request(HttpMethod.Get, "/servers/$serverId/members")
        return data!!.jsonObject
    }
}