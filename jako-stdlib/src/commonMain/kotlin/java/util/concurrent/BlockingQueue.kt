@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.util.Queue

expect interface BlockingQueue<E> : Queue<E> {
    fun put(e: E)
    fun take(): E
    fun poll(timeout: Long, unit: TimeUnit): E?
    fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    fun remainingCapacity(): Int
    fun drainTo(c: MutableCollection<in E>): Int
    fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
