package org.zeromq

interface Engine {

    val name: String

    fun createInstance(): EngineInstance
}
