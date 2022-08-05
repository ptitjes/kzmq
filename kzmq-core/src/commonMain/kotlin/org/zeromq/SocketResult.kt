/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

public sealed class SocketResult<out T> {
    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Failure

    public fun getOrNull(): T? = if (this is Success) this.value else null

    public fun getOrThrow(): T {
        if (this is Success) return this.value
        if (this is Failure && this.cause != null) throw this.cause
        error("Failed socket access")
    }

    public fun exceptionOrNull(): Throwable? = if (this is Failure) this.cause else null

    internal class Success<T>(val value: T) : SocketResult<T>()
    internal class Failure(val cause: Throwable?) : SocketResult<Nothing>()

    public companion object {
        public fun <T> success(value: T): SocketResult<T> = Success(value)
        public fun <T> failure(cause: Throwable? = null): SocketResult<T> = Failure(cause)
    }
}
