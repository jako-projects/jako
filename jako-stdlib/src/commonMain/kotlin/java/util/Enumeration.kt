@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface Enumeration<E> {
    fun hasMoreElements(): Boolean
    fun nextElement(): E
}
