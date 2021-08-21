package org.zeromq

interface XSubscriberSocket : Socket, ReceiveSocket {
    override val type: Type get() = Type.XSUB
}
