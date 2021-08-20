package org.zeromq

object JeroMQ : Engine {
    override val name = "jeromq"
    override fun createInstance(): EngineInstance = JeroMQInstance()
}
