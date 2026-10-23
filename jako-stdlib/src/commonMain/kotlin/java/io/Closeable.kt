@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface Closeable : AutoCloseable {
    override fun close()
}
