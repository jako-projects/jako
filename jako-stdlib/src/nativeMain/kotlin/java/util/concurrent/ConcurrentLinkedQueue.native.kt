package java.util.concurrent

import java.io.Serializable
import java.util.AbstractQueue
import java.util.Queue

actual open class ConcurrentLinkedQueue<E> : AbstractQueue<E>, Queue<E>, Serializable {
    private val list = ArrayList<E>()

    actual constructor()
    actual constructor(c: Collection<E>) {
        addAll(c)
    }

    actual override val size: Int
        get() = list.size

    actual override fun iterator(): MutableIterator<E> {
        val copy = ArrayList(list)
        val it = copy.iterator()
        return object : MutableIterator<E> {
            private var lastElem: E? = null

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val elem = it.next()
                lastElem = elem
                return elem
            }

            override fun remove() {
                val elem = lastElem
                if (elem != null) {
                    this@ConcurrentLinkedQueue.remove(elem)
                }
            }
        }
    }

    actual override fun offer(e: E): Boolean {
        if (e == null) throw NullPointerException()
        list.add(e)
        return true
    }

    override fun add(element: E): Boolean {
        return offer(element)
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
}
