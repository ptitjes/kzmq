@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS"
)

@file:JsModule("zeromq")
@file:JsNonModule

package zeromqjs

import Buffer
import kotlin.js.Promise

external interface Writable {
    var multicastHops: Number
    var sendBufferSize: Number
    var sendHighWaterMark: Number
    var sendTimeout: Number
    fun send(message: Array<Buffer>): Promise<Unit>
}

external interface Readable {
    var receiveBufferSize: Number
    var receiveHighWaterMark: Number
    var receiveTimeout: Number
    fun receive(): Promise<Array<Buffer>>
}

external interface `T$0` {
    var context: Context
}

//external interface EventSubscriber {
//    fun <E : Any> on(type: E, listener: (data: EventOfType<E>) -> Unit): EventSubscriber
//    fun <E : Any> off(type: E, listener: (data: EventOfType<E>) -> Unit): EventSubscriber
//}

external open class Pair() : Socket, Writable,
    Readable {
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
}

external open class Publisher() : Socket,
    Writable {
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var noDrop: Boolean
    open var conflate: Boolean
    open var invertMatching: Boolean
}

external open class Subscriber() : Socket,
    Readable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    open var conflate: Boolean
    open var invertMatching: Boolean
    open fun subscribe(vararg prefixes: Any /* Buffer | String */)
    open fun unsubscribe(vararg prefixes: Any /* Buffer | String */)
}

external open class Request() : Socket, Readable,
    Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var routingId: String?
    open var probeRouter: Boolean
    open var correlate: Boolean
    open var relaxed: Boolean
}

external open class Reply() : Socket, Readable,
    Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var routingId: String?
}

external open class Dealer() : Socket, Readable,
    Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var routingId: String?
    open var probeRouter: Boolean
    open var conflate: Boolean
}

external open class Router() : Socket, Readable,
    Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var routingId: String?
    open var mandatory: Boolean
    open var probeRouter: Boolean
    open var handover: Boolean
    open fun connect(address: String, options: RouterConnectOptions = definedExternally)
}

external interface RouterConnectOptions {
    var routingId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Pull() : Socket, Readable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    open var conflate: Boolean
}

external open class Push() : Socket, Writable {
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var conflate: Boolean
}

external open class XPublisher() : Socket,
    Readable, Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
    open var noDrop: Boolean
    open var manual: Boolean
    open var welcomeMessage: String?
    open var invertMatching: Boolean
}

external open class XSubscriber() : Socket,
    Readable, Writable {
    override var receiveBufferSize: Number
    override var receiveHighWaterMark: Number
    override var receiveTimeout: Number
    override fun receive(): Promise<Array<Buffer>>
    override var multicastHops: Number
    override var sendBufferSize: Number
    override var sendHighWaterMark: Number
    override var sendTimeout: Number
    override fun send(message: Array<Buffer>): Promise<Unit>
}
