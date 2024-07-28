/*
 * Copyright (c) 2021-2024 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS"
)

package org.zeromq.internal.zeromqjs

import Buffer
import kotlin.js.Promise

internal external interface `T$1` {
    var publicKey: String
    var secretKey: String
}

internal external fun curveKeyPair(): `T$1`

internal open external class Context() {
    open var blocky: Boolean
    open var ioThreads: Number
    open var maxMessageSize: Number
    open var maxSockets: Number
    open var ipv6: Boolean
    open var threadPriority: Number
    open var threadSchedulingPolicy: Number
    open var maxSocketsLimit: Number
    open fun getBoolOption(option: Number): Boolean
    open fun setBoolOption(option: Number, value: Boolean)
    open fun getInt32Option(option: Number): Number
    open fun setInt32Option(option: Number, value: Number)
}

internal typealias ErrnoError = Error

internal typealias AuthError = Error

internal typealias ProtoError = Error

internal external interface EventAddress {
    var address: String
}

internal external interface EventInterval {
    var interval: Number
}

internal external interface EventError<E> {
    var error: E
}

internal external interface EventError__0 : EventError<ErrnoError>

internal external interface `T$2`<T> {
    var type: T
}

//typealias Extract<T, U> = Any
//typealias Expand<T> = Any
//
//typealias EventFor<T, D> = Expand<`T$2`<T> /* `T$2`<T> & D */>
//
//typealias EventOfType<E> = Expand<Extract<dynamic /* EventFor<String /* "accept" */, EventAddress> | EventFor<String /* "accept:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "bind" */, EventAddress> | EventFor<String /* "bind:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "connect" */, EventAddress> | EventFor<String /* "connect:delay" */, EventAddress> | EventFor<String /* "connect:retry" */, EventAddress /* EventAddress & EventInterval */> | EventFor<String /* "close" */, EventAddress> | EventFor<String /* "close:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "disconnect" */, EventAddress> | EventFor<String /* "end" */> | EventFor<String /* "handshake" */, EventAddress> | EventFor<String /* "handshake:error:protocol" */, EventAddress /* EventAddress & EventError<ProtoError> */> | EventFor<String /* "handshake:error:auth" */, EventAddress /* EventAddress & EventError<AuthError> */> | EventFor<String /* "handshake:error:other" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "unknown" */> */, dynamic /* EventFor<String /* "accept" */, EventAddress> | EventFor<String /* "accept:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "bind" */, EventAddress> | EventFor<String /* "bind:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "connect" */, EventAddress> | EventFor<String /* "connect:delay" */, EventAddress> | EventFor<String /* "connect:retry" */, EventAddress /* EventAddress & EventInterval */> | EventFor<String /* "close" */, EventAddress> | EventFor<String /* "close:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "disconnect" */, EventAddress> | EventFor<String /* "end" */> | EventFor<String /* "handshake" */, EventAddress> | EventFor<String /* "handshake:error:protocol" */, EventAddress /* EventAddress & EventError<ProtoError> */> | EventFor<String /* "handshake:error:auth" */, EventAddress /* EventAddress & EventError<AuthError> */> | EventFor<String /* "handshake:error:other" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "unknown" */> */>>
//
//external open class Observer(socket: Socket) : EventSubscriber {
//    override fun <E : Any> on(type: E, listener: (data: EventOfType<E>) -> Unit): EventSubscriber
//    override fun <E : Any> off(type: E, listener: (data: EventOfType<E>) -> Unit): EventSubscriber
//    open var closed: Boolean
//    open fun close()
//    open fun receive(): Promise<dynamic /* EventFor<String /* "accept" */, EventAddress> | EventFor<String /* "accept:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "bind" */, EventAddress> | EventFor<String /* "bind:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "connect" */, EventAddress> | EventFor<String /* "connect:delay" */, EventAddress> | EventFor<String /* "connect:retry" */, EventAddress /* EventAddress & EventInterval */> | EventFor<String /* "close" */, EventAddress> | EventFor<String /* "close:error" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "disconnect" */, EventAddress> | EventFor<String /* "end" */> | EventFor<String /* "handshake" */, EventAddress> | EventFor<String /* "handshake:error:protocol" */, EventAddress /* EventAddress & EventError<ProtoError> */> | EventFor<String /* "handshake:error:auth" */, EventAddress /* EventAddress & EventError<AuthError> */> | EventFor<String /* "handshake:error:other" */, EventAddress /* EventAddress & EventError__0 */> | EventFor<String /* "unknown" */> */>
//}

internal open external class Proxy<F : Socket, B : Socket>(frontEnd: F, backEnd: B) {
    open var frontEnd: F
    open var backEnd: B
    open fun run(): Promise<Unit>
    open fun pause()
    open fun resume()
    open fun terminate()
}

internal open external class Socket(type: SocketType, options: Any = definedExternally) {
    open var affinity: Number
    open var rate: Number
    open var recoveryInterval: Number
    open var linger: Number
    open var reconnectInterval: Number
    open var backlog: Number
    open var reconnectMaxInterval: Number
    open var maxMessageSize: Number
    open var tcpKeepalive: Number
    open var tcpKeepaliveCount: Number
    open var tcpKeepaliveIdle: Number
    open var tcpKeepaliveInterval: Number
    open var tcpAcceptFilter: String?
    open var immediate: Boolean
    open var ipv6: Boolean
    open var plainServer: Boolean
    open var plainUsername: String?
    open var plainPassword: String?
    open var curveServer: Boolean
    open var curvePublicKey: String?
    open var curveSecretKey: String?
    open var curveServerKey: String?
    open var gssapiServer: Boolean
    open var gssapiPrincipal: String?
    open var gssapiServicePrincipal: String?
    open var gssapiPlainText: Boolean
    open var gssapiPrincipalNameType: String /* "hostBased" | "userName" | "krb5Principal" */
    open var gssapiServicePrincipalNameType: String /* "hostBased" | "userName" | "krb5Principal" */
    open var zapDomain: String?
    open var typeOfService: Number
    open var handshakeInterval: Number
    open var socksProxy: String?
    open var heartbeatInterval: Number
    open var heartbeatTimeToLive: Number
    open var heartbeatTimeout: Number
    open var connectTimeout: Number
    open var tcpMaxRetransmitTimeout: Number
    open var multicastMaxTransportDataUnit: Number
    open var vmciBufferSize: Number
    open var vmciBufferMinSize: Number
    open var vmciBufferMaxSize: Number
    open var vmciConnectTimeout: Number
    open var `interface`: String?
    open var zapEnforceDomain: Boolean
    open var loopbackFastPath: Boolean
    open var type: SocketType
    open var lastEndpoint: String?
    open var securityMechanism: String /* "plain" | "curve" | "gssapi" */
    open var threadSafe: Boolean

    //    open var events: Observer
    open var context: Context
    open var closed: Boolean
    open var readable: Boolean
    open var writable: Boolean
    open fun close()
    open fun bind(address: String): Promise<Unit>
    open fun unbind(address: String): Promise<Unit>
    open fun connect(address: String)
    open fun disconnect(address: String)
    open fun getBoolOption(option: Number): Boolean
    open fun setBoolOption(option: Number, value: Boolean)
    open fun getInt32Option(option: Number): Number
    open fun setInt32Option(option: Number, value: Number)
    open fun getUint32Option(option: Number): Number
    open fun setUint32Option(option: Number, value: Number)
    open fun getInt64Option(option: Number): Number
    open fun setInt64Option(option: Number, value: Number)
    open fun getUint64Option(option: Number): Number
    open fun setUint64Option(option: Number, value: Number)
    open fun getStringOption(option: Number): String?
    open fun setStringOption(option: Number, value: String?)
    open fun setStringOption(option: Number, value: Buffer?)
}

internal external enum class SocketType {
    Pair /* = 0 */,
    Publisher /* = 1 */,
    Subscriber /* = 2 */,
    Request /* = 3 */,
    Reply /* = 4 */,
    Dealer /* = 5 */,
    Router /* = 6 */,
    Pull /* = 7 */,
    Push /* = 8 */,
    XPublisher /* = 9 */,
    XSubscriber /* = 10 */,
    Stream /* = 11 */,
    Server /* = 12 */,
    Client /* = 13 */,
    Radio /* = 14 */,
    Dish /* = 15 */,
    Gather /* = 16 */,
    Scatter /* = 17 */,
    Datagram /* = 18 */
}
