package org.zeromq

sealed class SocketResult<out T> {
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = if (this is Success) this.value else null

    fun getOrThrow(): T {
        if (this is Success) return this.value
        if (this is Failure && this.cause != null) throw this.cause
        error("Failed socket access")
    }

    fun exceptionOrNull(): Throwable? = if (this is Failure) this.cause else null

    internal class Success<T>(val value: T) : SocketResult<T>()
    internal class Failure(val cause: Throwable?) : SocketResult<Nothing>()

    companion object {
        fun <T> success(value: T): SocketResult<T> = Success(value)
        fun <T> failure(cause: Throwable? = null): SocketResult<T> = Failure(cause)
    }
}
