package org.zeromq

import kotlin.coroutines.CoroutineContext

object Libzmq : Engine {
    override val name = "libzmq"
    override fun createInstance(coroutineContext: CoroutineContext): EngineInstance = LibzmqInstance()
}
