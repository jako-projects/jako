@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface Flushable {
    fun flush()
}
