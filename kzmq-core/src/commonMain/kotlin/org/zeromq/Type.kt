/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

import kotlin.js.*

public enum class Type(@JsName("__type") private val type: Int) {
    /**
     * Flag to specify an exclusive [PairSocket].
     * A [PairSocket] can only be connected to a single peer of type [PairSocket] at any one time.
     *
     * No message routing or filtering is performed on messages sent over a [PairSocket].
     *
     * When a [PairSocket] enters the mute state due to having reached the high watermark for the connected peer,
     * or if no peer is connected, then any [send()][SendSocket.send] operations on the socket shall suspend
     * until the peer becomes available for sending; messages are not discarded.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>PAIR</td></tr>
     * <tr><td>Direction</td><td>Bidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
     * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
     * <tr><td>Action in mute state</td><td>Suspend</td></tr>
     * </table><br/>
     *
     * **[PairSocket]s are designed for inter-thread communication across the `inproc` transport
     * and do not implement functionality such as auto-reconnection.
     * [PairSocket]s are considered experimental and may have other missing or broken aspects.**
     */
    PAIR(0),

    /**
     * Flag to specify a [PublisherSocket].
     * Peers must be [SubscriberSocket]s or [XSubscriberSocket]s.
     *
     * A [PublisherSocket] is used by a publisher to distribute data.
     *
     * Messages sent are distributed in a fan-out fashion to all connected peers.
     *
     * When a [PublisherSocket] enters the mute state due to having reached the high watermark for a subscriber,
     * then any messages that would be sent to the subscriber in question shall instead be dropped until the mute
     * state ends.
     *
     * The [send][SendSocket.send] methods shall never block for this socket type.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>SUB, XSUB</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Send only</td></tr>
     * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Fan-out</td></tr>
     * <tr><td>Action in mute state</td><td>Drop</td></tr>
     * </table><br/>
     */
    PUB(1),

    /**
     * Flag to specify [SubscriberSocket].
     * Peers must be [PublisherSocket]s or [XPublisherSocket]s.
     *
     * A [SubscriberSocket] is used by a subscriber to subscribe to data distributed by a publisher.
     *
     * Initially a [SubscriberSocket] is not subscribed to any messages.
     * Use [subscribe][SubscriberSocket.subscribe] methods to specify which messages to subscribe to.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>PUB, XPUB</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Receive only</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
     * </table><br/>
     */
    SUB(2),

    /**
     * Flag to specify a [RequestSocket].
     * Peers must be [ReplySocket]s or [RouterSocket]s.
     *
     * A [RequestSocket] is used by a client to send requests to and receive replies from a service.
     *
     * This socket type only allows an alternating sequence of [send()][SendSocket.send] and subsequent
     * [receive()][RequestSocket] calls.
     *
     * Each request sent is distributed in a round-robin fashion among all connected peers,
     * and each reply received is matched with the last issued request.
     *
     * If no services are available, then any [send][SendSocket.send] operation on the socket shall suspend
     * until at least one service becomes available.
     *
     * The [RequestSocket] shall not discard messages.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>REP, ROUTER</td></tr>
     * <tr><td>Direction</td><td>Bidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Send, Receive, Send, Receive, ...</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Last peer</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Round-robin</td></tr>
     * <tr><td>Action in mute state</td><td>Suspend</td></tr>
     * </table><br/>
     */
    REQ(3),

    /**
     * Flag to specify a [ReplySocket].
     * Peers must be [RequestSocket]s or [DealerSocket]s.
     *
     * A [ReplySocket] is used by a service to receive requests from and send replies to a client.
     *
     * This socket type only allows an alternating sequence of [send()][SendSocket.send] and subsequent
     * [receive()][SendSocket.receive] calls.
     *
     * Each request received is fair-queued from all connected peers,
     * and each reply sent is routed to the peer that issued the last request.
     *
     * If the original requester does not exist any more the reply is silently discarded.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>REQ, DEALER</td></tr>
     * <tr><td>Direction</td><td>Bidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Receive, Send, Receive, Send, ...</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Last peer</td></tr>
     * </table><br/>
     */
    REP(4),

    /**
     * Flag to specify a [DealerSocket].
     * Peers must be [ReplySocket]s or [RouterSocket]s.
     *
     * A [DealerSocket] does load-balancing on outputs and fair-queuing on inputs with no other semantics.
     * It is the only socket type that lets you shuffle messages out to N nodes and shuffle the replies back,
     * in a raw bidirectional asynchronous pattern.
     *
     * A [DealerSocket] is an advanced pattern used for extending request/reply sockets.
     *
     * Each message sent is distributed in a round-robin fashion among all connected peers,
     * and each message received is fair-queued from all connected peers.
     *
     * When a [DealerSocket] enters the mute state due to having reached the high watermark for all peers,
     * or if there are no peers at all, then any [send()][SendSocket.send] operations on the socket shall suspend
     * until the mute state ends or at least one peer becomes available for sending; messages are not discarded.
     *
     * When a [DealerSocket] is connected to a [ReplySocket] each message sent must consist of
     * an empty message frame, the delimiter, followed by one or more body parts.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>ROUTER, REP, DEALER</td></tr>
     * <tr><td>Direction</td><td>Bidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Round-robin</td></tr>
     * <tr><td>Action in mute state</td><td>Suspend</td></tr>
     * </table><br/>
     */
    DEALER(5),

    /**
     * Flag to specify [RouterSocket].
     * Peers must be [RequestSocket]s or [DealerSocket]s.
     *
     * A [RouterSocket] creates and consumes request-reply routing envelopes.
     * It is the only socket type that lets you route messages to specific connections if you know their identities.
     *
     * A [RouterSocket] is an advanced socket type used for extending request/reply sockets.
     *
     * When receiving messages a [RouterSocket] shall prepend a message frame containing the identity
     * of the originating peer to the message before passing it to the application.
     *
     * Messages received are fair-queued from all connected peers.
     *
     * When sending messages a [RouterSocket] shall remove the first frame of the message
     * and use it to determine the identity of the peer the message shall be routed to.
     * If the peer does not exist anymore the message shall be silently discarded by default,
     * unless [RouterSocket.mandatory] is set to `true`.
     *
     * When a [RouterSocket] enters the mute state due to having reached the high watermark for all peers,
     * then any messages sent to the socket shall be dropped until the mute state ends.
     *
     * Likewise, any messages routed to a peer for which the individual high watermark has been reached
     * shall also be dropped, unless [RouterSocket.mandatory] is set to true.
     *
     * When a [RequestSocket] is connected to a [RouterSocket], in addition to the identity of the originating peer
     * each message received shall contain an empty delimiter message frame.
     *
     * Hence, the entire structure of each received message as seen by the application becomes:
     * one or more identity frames, an empty delimiter frame, one or more body frames.
     *
     * When sending replies to a [RequestSocket] the application must include the empty delimiter frame.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>DEALER, REQ, ROUTER</td></tr>
     * <tr><td>Direction</td><td>Bidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Unrestricted</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>See text</td></tr>
     * <tr><td>Action in mute state</td><td>Drop (See text)</td></tr>
     * </table><br/>
     */
    ROUTER(6),

    /**
     * Flag to specify a [PullSocket].
     * Peers must be [PushSocket]s.
     *
     * A [PullSocket] is used by a pipeline node to receive messages from upstream pipeline nodes.
     *
     * Messages are fair-queued from all connected upstream nodes.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>PUSH</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Receive only</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
     * <tr><td>Action in mute state</td><td>Suspend</td></tr>
     * </table><br/>
     */
    PULL(7),

    /**
     * Flag to specify a [PushSocket].
     * Peers must be [PullSocket]s.
     *
     * A [PushSocket] is used by a pipeline node to send messages to downstream pipeline nodes.
     *
     * Messages are distributed round-robin fashion to all connected downstream nodes.
     *
     * When a [PushSocket] enters the mute state due to having reached the high watermark for all downstream nodes,
     * or if there are no downstream nodes at all, then any [send()][SendSocket.send] operations on the socket
     * shall suspend until the mute state ends or at least one downstream node becomes available for sending;
     * messages are not discarded.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>PULL</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Send only</td></tr>
     * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Round-robin</td></tr>
     * <tr><td>Action in mute state</td><td>Suspend</td></tr>
     * </table><br/>
     */
    PUSH(8),

    /**
     * Flag to specify a [XPublisherSocket], receiving side must be a [SubscriberSocket] or an [XSubscriberSocket].
     *
     * The behavior of an [XPublisherSocket] is the same as a [PublisherSocket],
     * except that you can receive subscription/unsubscription messages from the peers.
     *
     * Subscription messages contain a unique frame starting with a '1' byte.
     * Subscription cancellation messages contain a unique frame starting with a '0' byte.
     * Other messages are distributed as is.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>SUB, XSUB</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Send messages, receive subscriptions</td></tr>
     * <tr><td>Incoming routing strategy</td><td>N/A</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>Fan-out</td></tr>
     * <tr><td>Action in mute state</td><td>Drop</td></tr>
     * </table><br/>
     */
    XPUB(9),

    /**
     * Flag to specify a [XSubscriberSocket], sending side must be a [PublisherSocket] or an [XPublisherSocket].
     *
     * The behavior of an [XSubscriberSocket] is the same as a [SubscriberSocket],
     * except that you can send subscription/unsubscription messages to the peers.
     *
     * Subscription messages contain a unique frame starting with a '1' byte.
     * Subscription cancellation messages contain a unique frame starting with a '0' byte.
     * Other messages are distributed as is.
     *
     * <br/><table>
     * <tr><th colspan="2">Summary of socket characteristics</th></tr>
     * <tr><td>Compatible peer sockets</td><td>PUB, XPUB</td></tr>
     * <tr><td>Direction</td><td>Unidirectional</td></tr>
     * <tr><td>Send/receive pattern</td><td>Receive messages, send subscriptions</td></tr>
     * <tr><td>Incoming routing strategy</td><td>Fair-queued</td></tr>
     * <tr><td>Outgoing routing strategy</td><td>N/A</td></tr>
     * <tr><td>Action in mute state</td><td>Drop</td></tr>
     * </table><br/>
     */
    XSUB(10),

    ;

    public companion object {
        public fun type(baseType: Int): Type {
            for (type in values()) {
                if (type.type == baseType) {
                    return type
                }
            }
            throw IllegalArgumentException("no socket type found with value $baseType")
        }
    }
}
