package temp

import kotlinx.coroutines.*
import org.zeromq.*

fun main(): Unit = runBlocking {
    val handler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    val context = Context(CIO, coroutineContext + handler)

    context.pull {
        bind("ipc:///tmp/zmq-test")
    }
}

private suspend fun Context.pull(
    configure: SubscriberSocket.() -> Unit
) {
    with(createSubscriber()) {
        configure()
        subscribe()

        for (message in this) {
            val data = message.singleOrThrow().decodeToString()
            println("Received: $data")
        }
    }
}
