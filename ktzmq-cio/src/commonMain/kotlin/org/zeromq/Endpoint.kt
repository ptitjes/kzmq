package org.zeromq

internal sealed interface Endpoint

internal data class TCPEndpoint(
    val hostname: String,
    val port: Int,
    val path: String? = null
) : Endpoint

private const val TCP_PREFIX = "tcp://"

internal fun parseEndpoint(endpoint: String): Endpoint? {
    if (endpoint.startsWith(TCP_PREFIX)) {
        val withoutProtocol = endpoint.removePrefix(TCP_PREFIX)
        val noPath = !withoutProtocol.contains('/')

        val hostname = withoutProtocol.substringBefore(':')
        val beforePath = if (noPath) withoutProtocol else withoutProtocol.substringBefore('/')
        val port = beforePath.substringAfter(':').toInt()
        val path = if (noPath) null else "/${withoutProtocol.substringAfter('/')}"

        return TCPEndpoint(hostname, port, path)
    }

    return null
}
