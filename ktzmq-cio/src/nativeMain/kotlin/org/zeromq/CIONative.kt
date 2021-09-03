package org.zeromq

import kotlin.coroutines.*

actual object CIO : Engine {
    override val name = "cio"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance {
        return CIOInstance(coroutineContext)
    }
}
