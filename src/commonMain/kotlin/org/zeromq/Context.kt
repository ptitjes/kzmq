package org.zeromq

expect class Context {

    constructor()
    constructor(ioThreads: Int)

    fun createSocket(type: Type): Socket

    companion object {
        fun shadow(context: Context): Context
    }
}
