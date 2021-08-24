package org.zeromq

import kotlin.coroutines.CoroutineContext

interface Engine {

    val name: String

    fun createInstance(coroutineContext: CoroutineContext): EngineInstance
}
