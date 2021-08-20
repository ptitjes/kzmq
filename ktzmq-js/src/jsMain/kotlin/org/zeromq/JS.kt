package org.zeromq

object JS : Engine {
    override val name = "zeromq.js"
    override fun createInstance(): EngineInstance = JSInstance()
}
