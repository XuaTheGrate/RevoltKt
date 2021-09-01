package me.maya.revolt

import kotlinx.coroutines.runBlocking
import me.maya.revolt.api.User
import me.maya.revolt.events.Event
import me.maya.revolt.events.EventHandler
import kotlin.properties.Delegates
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class Client {
    internal val state = State()
    internal val http = HttpClient(state)
    internal val gateway = Gateway(http)

    internal var errorCallback: (suspend (Event, Throwable) -> Unit)? = null

    suspend fun fetchUser(id: String): User = TODO()

    fun runForever() = runBlocking<Unit> {
        gateway.start()
    }

    open suspend fun close() {
        gateway.stopEvent.complete(Unit)
    }
}

class ClientBuilder<T: Client> @PublishedApi internal constructor() {
    @Suppress("UNCHECKED_CAST")
    internal var clientClass: KClass<T> = Client::class as KClass<T>

    internal val eventHandlers = mutableListOf<EventHandler>()
    internal var token: String by Delegates.notNull()
    internal var errorCallback: (suspend (Event, Throwable) -> Unit)? = null

    internal var builtClient: T? = null

    val client: T get() = builtClient ?: throw IllegalStateException("client has not been built yet")

    fun build(): T {
        if (builtClient != null) throw IllegalStateException("builder has already been built")
        builtClient = clientClass.primaryConstructor!!.call()

        client.http.token = token
        client.gateway.eventHandlers.addAll(eventHandlers)

        return client
    }
}

fun ClientBuilder<*>.token(token: String) {
    this.token = token
}

fun ClientBuilder<*>.setErrorCallback(block: suspend (Event, Throwable) -> Unit) {
    errorCallback = block
}

fun ClientBuilder<*>.registerEventHandler(obj: EventHandler) {
    eventHandlers.add(obj)
}

fun defaultClientBuilder(block: ClientBuilder<Client>.() -> Unit): ClientBuilder<Client> {
    return clientBuilder(block)
}

inline fun <reified T: Client> clientBuilder(block: ClientBuilder<T>.() -> Unit): ClientBuilder<T> {
    val builder = ClientBuilder<T>()
    block(builder)
    return builder
}

