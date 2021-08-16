package org.zeromq

object JeroMQ : Engine {
    override fun createInstance(): EngineInstance = JeroMQInstance()
}
