@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.Closeable

expect interface Channel : Closeable {
    fun isOpen(): Boolean
    override fun close()

    val isOpen: Boolean
}
