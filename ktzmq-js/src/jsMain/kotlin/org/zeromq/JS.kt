package org.zeromq

object JS : Engine {
    override fun createInstance(): EngineInstance = JSInstance()
}
