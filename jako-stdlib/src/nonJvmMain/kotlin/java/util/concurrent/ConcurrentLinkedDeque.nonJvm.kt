package java.util.concurrent

import java.io.Serializable
import java.util.Deque
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class ConcurrentLinkedDeque<E> : AbstractMutableCollection<E>, Deque<E>, Serializable {
    private val lock = ReentrantLock()
    private val items = ArrayList<E>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        for (e in c) {
            if (e == null) throw NullPointerException()
            items.add(e)
        }
    }

    private fun checkNotNullElement(e: Any?) {
        if (e == null) throw NullPointerException()
    }

    actual override val size: Int
        get() = lock.withLock { items.size }

    actual override fun isEmpty(): Boolean = lock.withLock { items.isEmpty() }

    actual override fun contains(element: E): Boolean {
        if (element == null) return false
        return lock.withLock { items.contains(element) }
    }

    actual override fun add(element: E): Boolean = offerLast(element)

    actual override fun remove(element: E): Boolean = removeFirstOccurrence(element)

    actual override fun clear() = lock.withLock { items.clear() }

    actual override fun addFirst(e: E) {
        checkNotNullElement(e)
        lock.withLock { items.add(0, e) }
    }

    actual override fun addLast(e: E) {
        checkNotNullElement(e)
        lock.withLock { items.add(e) }
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

    actual override fun pollFirst(): E? = lock.withLock {
        if (items.isEmpty()) null else items.removeAt(0)
    }

    actual override fun pollLast(): E? = lock.withLock {
        if (items.isEmpty()) null else items.removeAt(items.size - 1)
    }

    actual override fun getFirst(): E = peekFirst() ?: throw NoSuchElementException()

    actual override fun getLast(): E = peekLast() ?: throw NoSuchElementException()

    actual override fun peekFirst(): E? = lock.withLock {
        if (items.isEmpty()) null else items[0]
    }

    actual override fun peekLast(): E? = lock.withLock {
        if (items.isEmpty()) null else items[items.size - 1]
    }

    actual open fun removeFirstOccurrence(o: Any?): Boolean {
        if (o == null) return false
        return lock.withLock {
            val idx = items.indexOfFirst { it == o }
            if (idx >= 0) {
                items.removeAt(idx)
                true
            } else {
                false
            }
        }
    }

    actual open fun removeLastOccurrence(o: Any?): Boolean {
        if (o == null) return false
        return lock.withLock {
            val idx = items.indexOfLast { it == o }
            if (idx >= 0) {
                items.removeAt(idx)
                true
            } else {
                false
            }
        }
    }

    actual override fun offer(e: E): Boolean = offerLast(e)

    actual open fun remove(): E = removeFirst()

    actual override fun poll(): E? = pollFirst()

    actual open fun element(): E = getFirst()

    actual override fun peek(): E? = peekFirst()

    actual override fun push(e: E) = addFirst(e)

    actual override fun pop(): E = removeFirst()

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = lock.withLock { ArrayList(items) }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                removeFirstOccurrence(e)
            }
        }
    }

    actual override fun descendingIterator(): MutableIterator<E> {
        val snapshot = lock.withLock {
            val list = ArrayList(items)
            list.reverse()
            list
        }
        return object : MutableIterator<E> {
            private val it = snapshot.iterator()
            private var lastRet: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val e = it.next()
                lastRet = e
                return e
            }
            override fun remove() {
                val e = lastRet ?: throw IllegalStateException()
                lastRet = null
                removeLastOccurrence(e)
            }
        }
    }
}
