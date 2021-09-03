package org.zeromq

import kotlinx.coroutines.*
import kotlin.coroutines.*

actual object CIO : Engine {
    override val name = "cio"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        CIOInstance(coroutineContext + Dispatchers.IO)
}
