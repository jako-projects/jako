package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.Deque
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

actual interface BlockingDeque<E> : BlockingQueue<E>, Deque<E>

actual open class LinkedBlockingDeque<E> : AbstractQueue<E>, BlockingDeque<E>, Serializable {
    private val list = ArrayList<E>()

    actual constructor()
    actual constructor(capacity: Int)
    actual constructor(c: Collection<E>) {
        addAll(c)
    }

    actual override val size: Int
        get() = list.size

    actual override fun iterator(): MutableIterator<E> {
        val copy = ArrayList(list)
        return copy.iterator()
    }

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        list.add(e)
        return true
    }

    actual override fun put(e: E) {
        offer(e)
    }

    actual override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean = offer(e)
    actual override fun remainingCapacity(): Int = Int.MAX_VALUE
    actual override fun drainTo(c: MutableCollection<in E>): Int = drainTo(c, Int.MAX_VALUE)
    actual override fun drainTo(c: MutableCollection<in E>, maxElements: Int): Int {
        var n = 0
        while (n < maxElements && isNotEmpty()) {
            val el = poll() ?: break
            c.add(el)
            n++
        }
        return n
    }

    actual override fun take(): E {
        return poll() ?: throw NoSuchElementException()
    }

    actual override fun poll(timeout: Long, unit: TimeUnit): E? {
        return poll()
    }

    actual override fun poll(): E? {
        if (list.isEmpty()) return null
        return list.removeAt(0)
    }

    actual override fun peek(): E? {
        return list.firstOrNull()
    }

    override fun remove(element: E): Boolean {
        return list.remove(element)
    }

    override fun clear() {
        list.clear()
    }

    actual override fun addFirst(e: E) {
        if (e == null) throw NullPointerException()
        list.add(0, e)
    }

    actual override fun addLast(e: E) {
        if (e == null) throw NullPointerException()
        list.add(e)
    }

    actual override fun offerFirst(e: E): Boolean {
        addFirst(e)
        return true
    }

    actual override fun offerLast(e: E): Boolean {
        addLast(e)
        return true
    }

    actual override fun removeFirst(): E = pollFirst() ?: throw NoSuchElementException()

    actual override fun removeLast(): E = pollLast() ?: throw NoSuchElementException()

    actual override fun pollFirst(): E? = if (list.isEmpty()) null else list.removeAt(0)

    actual override fun pollLast(): E? = if (list.isEmpty()) null else list.removeAt(list.size - 1)

    actual override fun getFirst(): E = peekFirst() ?: throw NoSuchElementException()

    actual override fun getLast(): E = peekLast() ?: throw NoSuchElementException()

    actual override fun peekFirst(): E? = list.firstOrNull()

    actual override fun peekLast(): E? = list.lastOrNull()

    actual override fun pop(): E = removeFirst()

    actual override fun push(e: E) = addFirst(e)

    actual override fun descendingIterator(): MutableIterator<E> {
        val copy = ArrayList(list)
        copy.reverse()
        return copy.iterator()
    }
}
