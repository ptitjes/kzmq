/*
 * Copyright (c) 2021-2022 Didier Villevalois and Kzmq contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.zeromq.tests.utils

import org.zeromq.*
import kotlin.math.*

object FrameComparator : Comparator<Frame> {
    override fun compare(a: Frame, b: Frame): Int {
        for (i in 0 until min(a.size, b.size)) {
            if (a[i] != b[i]) {
                return b[i] - a[i]
            }
        }
        return b.size - a.size
    }
}
