/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq

/**
 * A ZeroMQ backend engine. Should not be used directly. Use [Context] instead.
 */
public interface Engine : SocketFactory, Closeable
