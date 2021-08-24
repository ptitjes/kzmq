package org.zeromq

import io.ktor.network.selector.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext

/**
 * An implementation of [ZeroMQ Exclusive Pair](https://rfc.zeromq.org/spec/31/)
 *
 * ## Overall Goals of this Pattern
 * PAIR is not a general-purpose socket but is intended for specific use cases where the two peers
 * are architecturally stable. This usually limits PAIR to use within a single process, for
 * inter-thread communication.
 *
 * ## The PAIR Socket Type
 *
 * A. General behavior:
 * 1. MAY be connected to at most one PAIR peers, and MAY both send and receive messages.
 * 2. SHALL not filter or modify outgoing or incoming messages in any way.
 * 3. SHALL maintain a double queue for its peer, allowing outgoing and incoming messages to be
 *    queued independently.
 * 4. SHALL create a double queue when initiating an outgoing connection to a peer, and SHALL
 *    maintain the double queue whether or not the connection is established.
 * 5. SHALL create a double queue when a peer connects to it. If this peer disconnects, the PAIR
 *    socket SHALL destroy its double queue and SHALL discard any messages it contains.
 * 6. SHOULD constrain incoming and outgoing queue sizes to a runtime-configurable limit.
 *
 * B. For processing outgoing messages:
 * 1. SHALL consider its peer as available only when it has a outgoing queue that is not full.
 * 2. SHALL block on sending, or return a suitable error, when it has no available peer.
 * 3. SHALL not accept further messages when it has no available peer.
 * 4. SHALL NOT discard messages that it cannot queue.
 *
 * C. For processing incoming messages:
 * 1. SHALL receive incoming messages from its single peer if it has one.
 * 2. SHALL deliver these to its calling application.
 */
internal class CIOPairSocket(
    coroutineContext: CoroutineContext,
    selectorManager: SelectorManager
) : CIOSocket(coroutineContext, selectorManager, Type.PAIR),
    CIOSendSocket,
    CIOReceiveSocket,
    PairSocket {

    override val sendChannel = Channel<Message>()
    override val receiveChannel = Channel<Message>()

    // TODO parameterize CIOSocket to be able to enforce A.1.

    // Use rawSocketActions to receive the addition and removal of raw sockets

    // Buffer sendChannel and receiveChannel to enforce A.3. and A.4.
    // Does A.4. means we have to know when connect(...) starts ?

    // Can A.5. be handled by recreating sendChannel and receiveChannel
    // when the rawSocket is removed ?

    // A.6. means honoring sendHighWaterMark and receiveHighWaterMark, right?

    // B.1-4 are automatically handled by setting the capacity of
    // sendChannel to sendHighWaterMark. sendChannel with suspend when this
    // limit has been reached

    // C.1-2 are obviously handled
}
