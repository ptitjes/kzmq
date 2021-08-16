package org.zeromq

interface Engine {
    fun createInstance(): EngineInstance
}
