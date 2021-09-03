package temp

import kotlinx.coroutines.*
import org.zeromq.SocketType
import org.zeromq.ZContext

fun main(): Unit = runBlocking {
    val context = ZContext()

    launch {
        val publisher = context.createSocket(SocketType.PUB)
        publisher.connect("tcp://localhost:9990")
        publisher.sndHWM = 1000

        var i = 0
        while (true) {
            val str = "$i"
            withContext(Dispatchers.IO) { publisher.send(str) }
            println("Sent: $str")
            delay(1000)
            i++
        }
    }

//    launch {
//        val subscriber = context.createSocket(SocketType.SUB)
//        subscriber.bind("tcp://localhost:9990")
//        subscriber.subscribe("")
//
//        while (true) {
//            val str = withContext(Dispatchers.IO) { subscriber.recvStr() }
//            println("Received: $str")
//        }
//    }
}
