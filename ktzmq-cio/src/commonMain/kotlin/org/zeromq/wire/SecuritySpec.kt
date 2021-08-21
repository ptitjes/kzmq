package org.zeromq.wire

internal data class SecuritySpec(val mechanism: Mechanism, val asServer: Boolean)
