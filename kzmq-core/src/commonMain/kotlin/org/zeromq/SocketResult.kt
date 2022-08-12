/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A discriminated union of socket operation result.
 * It encapsulates the successful or failed result of a socket operation.
 *
 * The successful result represents a successful operation with a value of type [T],
 * for example, the result of [ReceiveSocket.receiveCatching] operation
 * or a successfully sent element as a result of [SendSocket.trySend].
 *
 * The failed result represents a failed operation attempt to a socket,
 * but it doesn't necessarily indicate that the socket is failed.
 * E.g. when the socket is full, [SendSocket.trySend] returns failed result,
 * but the socket itself is not in the failed state.
 */
public sealed class SocketResult<out T> {

    /**
     * Returns [true] if this instance represents a successful operation outcome.
     */
    public val isSuccess: Boolean get() = this is Success

    /**
     * Returns [true] if this instance represents unsuccessful operation.
     */
    public val isFailure: Boolean get() = this is Failure

    /**
     * Returns the encapsulated value if this instance represents success or `null` if it represents failed result.
     */
    public fun getOrNull(): T? = if (this is Success) this.value else null

    /**
     * Returns the encapsulated value if this instance represents success or throws an exception if it is closed or failed.
     */
    public fun getOrThrow(): T {
        if (this is Success) return this.value
        if (this is Failure && this.cause != null) throw this.cause
        error("Failed socket access")
    }

    /**
     * Returns the encapsulated exception if this instance represents failure
     * or `null` if it is a successful operation.
     */
    public fun exceptionOrNull(): Throwable? = if (this is Failure) this.cause else null

    internal class Success<T>(val value: T) : SocketResult<T>()
    internal class Failure(val cause: Throwable?) : SocketResult<Nothing>()

    public companion object {
        public fun <T> success(value: T): SocketResult<T> = Success(value)
        public fun <T> failure(cause: Throwable? = null): SocketResult<T> = Failure(cause)
    }
}
