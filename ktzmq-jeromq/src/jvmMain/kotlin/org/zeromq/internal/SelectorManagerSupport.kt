/*
 * Copyright (c) 2021-2021 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */
package org.zeromq.internal

import kotlinx.coroutines.*
import java.nio.channels.*
import java.nio.channels.spi.*
import kotlin.coroutines.*

/**
 * Base class for NIO selector managers
 */
internal abstract class SelectorManagerSupport internal constructor() : SelectorManager {
    val provider: SelectorProvider = SelectorProvider.provider()

    /**
     * Number of pending selectables
     */
    protected var pending: Int = 0

    /**
     * Number of cancelled keys
     */
    protected var cancelled: Int = 0

    /**
     * Publish current [selectable] interest, any thread
     */
    protected abstract fun publishInterest(selectable: Selectable)

    override suspend fun suspendForSelection(selectable: Selectable, interest: SelectInterest) {
        suspendCancellableCoroutine<Unit> { continuation ->
            continuation.invokeOnCancellation {
                selectable.trace("cancelling poller registration")

                if (selectable.interestOp(interest, false) == 0) {
                    selectable.trace("removing from cancellation")
                    selectable.suspensions.removeSuspension(interest)
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

    /**
     * Handle selected keys clearing [selectedKeys] set
     */
    protected fun handleSelectedKeys(
        selectedKeys: MutableSet<SelectionKey>,
        keys: Set<SelectionKey>
    ) {
        val selectedCount = selectedKeys.size
        pending = keys.size - selectedCount
        cancelled = 0

        if (selectedCount > 0) {
            val iter = selectedKeys.iterator()
            while (iter.hasNext()) {
                val k = iter.next()
                handleSelectedKey(k)
                iter.remove()
            }
        }
    }

    /**
     * Handles particular selected key
     */
    private fun handleSelectedKey(key: SelectionKey) {
        try {
            val readyOps = key.readyOps()
            val interestOps = key.interestOps()

            val subj = key.subject
            if (subj == null) {
                key.cancel()
                cancelled++
            } else {
                val unit = Unit

                subj.suspensions.invokeForEachPresent(subj.readyOps) { resume(unit) }
                subj.interestOps(subj.readyOps, false)

                val newOps = interestOps and readyOps.inv()
                if (newOps != interestOps) {
                    key.interestOps(newOps)
                }

                if (newOps != 0) {
                    pending++
                }
            }
        } catch (t: Throwable) {
            // cancelled or rejected on resume?
            key.cancel()
            cancelled++
            key.subject?.let { subj ->
                cancelAllSuspensions(subj, t)
                key.subject = null
            }
        }
    }

    /**
     * Applies selectable's current interest (should be invoked in selection thread)
     */
    protected fun applyInterest(selector: Selector, s: Selectable) {
        try {
            val channel = s.channel
            val key = channel.keyFor(selector)
            val ops = s.interestOps

            if (key == null) {
                if (ops != 0) {
                    channel.register(selector, ops, s)
                }
            } else {
                if (key.interestOps() != ops) {
                    key.interestOps(ops)
                }
            }

            if (ops != 0) {
                pending++
            }
        } catch (t: Throwable) {
            s.channel.keyFor(selector)?.cancel()
            cancelAllSuspensions(s, t)
        }
    }

    /**
     * Cancel all selectable's suspensions with the specified exception
     */
    protected fun cancelAllSuspensions(attachment: Selectable, t: Throwable) {
        attachment.suspensions.invokeForEachPresent {
            resumeWithException(t)
        }
    }

    /**
     * Cancel all suspensions with the specified exception, reset all interests
     */
    protected fun cancelAllSuspensions(selector: Selector, t: Throwable?) {
        val cause = t ?: ClosedSelectorCancellationException()

        selector.keys().forEach { k ->
            try {
                if (k.isValid) k.interestOps(0)
            } catch (ignore: CancelledKeyException) {
            }
            (k.attachment() as? Selectable)?.let { cancelAllSuspensions(it, cause) }
            k.cancel()
        }
    }

    private var SelectionKey.subject: Selectable?
        get() = attachment() as? Selectable
        set(newValue) {
            attach(newValue)
        }

    class ClosedSelectorCancellationException : CancellationException("Closed selector")
}
