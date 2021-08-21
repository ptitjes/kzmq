package org.zeromq

interface PairSocket : Socket, SendSocket, ReceiveSocket {
    override val type: Type get() = Type.PAIR
}
