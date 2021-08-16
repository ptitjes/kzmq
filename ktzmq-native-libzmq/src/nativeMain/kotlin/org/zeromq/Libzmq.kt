package org.zeromq

object Libzmq : Engine {
    override fun createInstance(): EngineInstance = LibzmqInstance()
}
