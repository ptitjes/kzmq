package org.zeromq.internal

import kotlinx.coroutines.suspendCancellableCoroutine
import zmq.ZError
import zmq.util.Clock
import java.io.IOException
import java.nio.channels.*
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.resume

internal class SelectorManager() {
    private val provider: SelectorProvider = SelectorProvider.provider()

    private val selectables = hashSetOf<Selectable>()

    init {
        val pollLoop = {
            val selector = provider.openSelector() ?: error("openSelector() = null")

            while (true) {
                val polled = selectables.toTypedArray()

                val count = poll(selector, polled, -1)
                if (count == -1) {
                    TODO("handle context close")
                }

                for (selectable in polled) {
                    val readyOps = selectable.readyOps
                    if (readyOps == 0) continue

                    selectable.debug("before invokeForEachPresent($readyOps)")
                    selectable.suspensions.invokeForEachPresent(readyOps) { resume(Unit) }
                    selectable.debug("after invokeForEachPresent($readyOps)")

                    if (selectable.interestOps(readyOps, false) == 0) {
                        selectable.trace("removing from poll")
                        selectables.remove(selectable)
                    }
                }
            }
        }

        Thread(pollLoop, "ktzmq-jeromq-poller").start()
    }

    private fun poll(selector: Selector, items: Array<Selectable>, timeout: Long): Int {
        val count = items.size
        if (count == 0) {
            if (timeout <= 0) {
                return 0
            }
            LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(timeout, TimeUnit.MILLISECONDS))
            return 0
        }

        var now = 0L
        var end = 0L
        val saved = HashMap<SelectableChannel, SelectionKey>()
        for (key in selector.keys()) {
            if (key.isValid) {
                saved[key.channel()] = key
            }
        }
        for (i in 0 until count) {
            val item = items[i]
            val ch = item.channel // mailbox channel if ZMQ socket
            val key = saved.remove(ch)
            if (key != null) {
                if (key.interestOps() != item.interestOps) {
                    key.interestOps(item.interestOps)
                }
                key.attach(item)
            } else {
                try {
                    ch.register(selector, item.interestOps, item)
                } catch (e: ClosedSelectorException) {
                    // context was closed asynchronously, exit gracefully
                    return -1
                } catch (e: ClosedChannelException) {
                    throw ZError.IOException(e)
                }
            }
        }
        if (saved.isNotEmpty()) {
            for (deprecated in saved.values) {
                deprecated.cancel()
            }
        }

        var firstPass = true
        var nevents = 0
        var ready: Int
        while (true) {
            //  Compute the timeout for the subsequent poll.
            var waitMillis: Long
            if (firstPass) {
                waitMillis = 0L
            } else if (timeout < 0L) {
                waitMillis = -1L
            } else {
                waitMillis = TimeUnit.NANOSECONDS.toMillis(end - now)
                if (waitMillis == 0L) {
                    waitMillis = 1L
                }
            }

            //  Wait for events.
            try {
                if (waitMillis < 0) {
                    selector.select(0)
                } else if (waitMillis == 0L) {
                    selector.selectNow()
                } else {
                    selector.select(waitMillis)
                }

                for (key in selector.keys()) {
                    val item = key.attachment() as Selectable
                    ready = item.readyOps
                    if (ready < 0) {
                        return -1
                    }
                    if (ready > 0) {
                        nevents++
                    }
                }
                selector.selectedKeys().clear()
            } catch (e: ClosedSelectorException) {
                // context was closed asynchronously, exit gracefully
                return -1
            } catch (e: IOException) {
                throw ZError.IOException(e)
            }
            //  If timeout is zero, exit immediately whether there are events or not.
            if (timeout == 0L) {
                break
            }
            if (nevents > 0) {
                break
            }

            //  At this point we are meant to wait for events but there are none.
            //  If timeout is infinite we can just loop until we get some events.
            if (timeout < 0) {
                if (firstPass) {
                    firstPass = false
                }
                continue
            }

            //  The timeout is finite and there are no events. In the first pass
            //  we get a timestamp of when the polling have begun. (We assume that
            //  first pass have taken negligible time). We also compute the time
            //  when the polling should time out.
            if (firstPass) {
                now = Clock.nowNS()
                end = now + TimeUnit.MILLISECONDS.toNanos(timeout)
                if (now == end) {
                    break
                }
                firstPass = false
                continue
            }

            //  Find out whether timeout have expired.
            now = Clock.nowNS()
            if (now >= end) {
                break
            }
        }
        return nevents
    }

    suspend fun suspendForSelection(selectable: Selectable, interest: SelectInterest) {
        suspendCancellableCoroutine<Unit> { continuation ->
            continuation.invokeOnCancellation {
                selectable.trace("cancelling poller registration")

                if (selectable.interestOp(interest, false) == 0) {
                    selectable.trace("removing from cancellation")
                    // TODO
                    // socketToSelectable.remove(selectable.socket)
                }
            }

            selectable.trace("registering interest $interest")
            selectable.suspensions.addSuspension(interest, continuation)

            if (!continuation.isCancelled) {
                selectable.interestOp(interest, true)
                publishInterest(selectable)
            }
        }
    }

    private fun publishInterest(selectable: Selectable) {
        selectables.add(selectable)
    }
}
