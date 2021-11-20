package org.zeromq

import kotlin.coroutines.*

object JeroMQ : Engine {
    override val name = "jeromq"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        JeroMQInstance()
}
