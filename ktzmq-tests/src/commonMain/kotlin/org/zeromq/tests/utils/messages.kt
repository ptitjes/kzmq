package org.zeromq.tests.utils

import org.zeromq.*

object MessageComparator : Comparator<Message> {
    override fun compare(a: Message, b: Message): Int {
        val aAsString = a.parts.joinToString { it.decodeToString() }
        val bAsString = b.parts.joinToString { it.decodeToString() }
        return if (aAsString < bAsString) -1
        else if (aAsString > bAsString) +1
        else 0
    }
}
