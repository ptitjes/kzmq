package org.zeromq

import kotlin.coroutines.*

interface Engine {

    val name: String

    fun createInstance(coroutineContext: CoroutineContext): EngineInstance
}
