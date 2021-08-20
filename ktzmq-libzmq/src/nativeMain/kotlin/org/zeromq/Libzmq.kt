package org.zeromq

object Libzmq : Engine {
    override val name = "libzmq"
    override fun createInstance(): EngineInstance = LibzmqInstance()
}
