package java.util.concurrent

import java.util.Queue

actual interface BlockingQueue<E> : Queue<E> {
    actual fun put(e: E)
    actual fun take(): E
    actual fun poll(timeout: Long, unit: TimeUnit): E?
    actual fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean
    actual fun remainingCapacity(): Int
    actual fun drainTo(c: MutableCollection<in E>): Int
    actual fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int
}
