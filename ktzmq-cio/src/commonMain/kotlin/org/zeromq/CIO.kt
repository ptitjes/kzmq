package org.zeromq

import kotlin.coroutines.CoroutineContext

object CIO : Engine {
    override val name = "cio"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance =
        CIOInstance(coroutineContext)
}
