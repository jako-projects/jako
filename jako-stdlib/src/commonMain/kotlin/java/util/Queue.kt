@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface Queue<E> : MutableCollection<E> {
    fun offer(e: E): Boolean
    fun poll(): E?
    fun peek(): E?
}
