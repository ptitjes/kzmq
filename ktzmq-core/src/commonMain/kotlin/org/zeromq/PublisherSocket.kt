package org.zeromq

interface PublisherSocket : Socket, SendSocket {
    override val type: Type get() = Type.PUB
}
