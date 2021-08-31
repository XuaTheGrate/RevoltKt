package me.maya.revolt

import com.mayak.json.Json
import com.mayak.json.JsonObject
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import me.maya.revolt.errors.RevoltError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.seconds

class Gateway internal constructor(val http: HttpClient) {
    val logger: Logger = LoggerFactory.getLogger(Gateway::class.java)

    val state = http.state
    var gateway: DefaultClientWebSocketSession by Delegates.notNull()
    val stopEvent = CompletableDeferred<Unit>()
    val errorHandler = CoroutineExceptionHandler { ctx, err ->
        logger.error("Error occured during event handling", err)
    }

    var lastResponseTime: Long by Delegates.notNull(); private set
    private var pongOccured = CompletableDeferred<Unit>()
    private var pingJob: Job? = null
    private var readyEvent = CompletableDeferred<Unit>()

    suspend fun recvLoop() {
        gateway.incoming.receiveAsFlow().collect {
            val data: String = (it as Frame.Text).readText()
            val resp = Json.parse(data).jsonObject
            GlobalScope.launch { dispatch(resp) }
        }
    }

    suspend fun dispatch(data: JsonObject) {
        logger.info("RECV: ${Json.stringify(data)}")

        when (data["type"].string) {
            "Pong" -> { // currently bugged
                // pongOccured.complete(Unit)
            }
            "Ready" -> {
                readyEvent.complete(Unit)
            }
        }
    }

    suspend fun pingLoop(): Nothing {
        while (true) {
            delay(Duration.seconds(30))
            // GlobalScope.launch {
                send(Json.createObject(mapOf("type" to "Ping", "time" to 0)))
            // }

            // val time = System.currentTimeMillis()
            // pongOccured.await()
            // lastResponseTime = System.currentTimeMillis() - time
            // pongOccured = CompletableDeferred()

        }
    }

    suspend fun send(data: Map<String, Any?>) {
        send(Json.createObject(data))
    }

    suspend fun send(data: JsonObject) {
        send(Json.stringify(data))
    }

    suspend fun send(data: String) {
        // println("SENDING: $data")
        logger.info("SEND: $data")
        gateway.send(data)
    }

    suspend fun authenticate() {
        send(mapOf(
            "type" to "Authenticate",
            "token" to http.token
        ))
    }

    suspend fun start() {
        state.load()

        http.client.wss(state.ws) {
            // println("GATEWAY: connected successfully")
            logger.info("Connected successfully")
            gateway = this
            authenticate()

            val msg = incoming.receive().readBytes().decodeToString().let(Json::parse).jsonObject
            when (val t = msg["type"].string) {
                "Error" -> {
                    val err = when (val e = msg["error"].string) {
                        "LabelMe" -> RevoltError.Gateway.LabelMe()
                        "InternalError" -> RevoltError.Gateway.InternalError()
                        "InvalidSession" -> RevoltError.Gateway.InvalidSession()
                        "OnboardingNotFinished" -> RevoltError.Gateway.OnboardingNotFinished()
                        "AlreadyAuthenticated" -> RevoltError.Gateway.AlreadyAuthenticated()
                        else -> RevoltError.Gateway.UnknownGatewayError("Unknown gateway error type occured: $e")
                    }
                    throw err
                }
                "Authenticated" -> {}
                else -> throw IllegalStateException("unexpected response type from gateway: $t")
            }
            // println("GATEWAY: authentication successful")
            logger.info("Authentication successful")

            // pingJob = GlobalScope.launch { pingLoop() }

            val job = GlobalScope.launch(errorHandler) {
                recvLoop()
            }
            // println("GATEWAY: recv loop started, awaiting exit")
            logger.info("Receive loop started, ready to dispatch events")
            stopEvent.await()
            logger.info("Stop event complete, shutting down")
            job.cancelAndJoin()
            pingJob?.cancelAndJoin()
        }
    }
}