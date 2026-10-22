package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class LinkedList<E> : AbstractSequentialList<E>, MutableList<E>, Deque<E>, Cloneable, Serializable {
    private val list = ArrayList<E>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        list.addAll(c)
    }

    actual override val size: Int
        get() = list.size

    actual override fun getFirst(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[0]
    }

    actual override fun getLast(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[list.size - 1]
    }

    actual override fun removeFirst(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list.removeAt(0)
    }

    actual override fun removeLast(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list.removeAt(list.size - 1)
    }

    actual override fun addFirst(e: E) {
        list.add(0, e)
    }

    actual override fun addLast(e: E) {
        list.add(e)
    }

    actual override fun contains(element: E): Boolean = list.contains(element)

    actual override fun add(element: E): Boolean = list.add(element)

    actual override fun remove(element: E): Boolean = list.remove(element)

    actual override fun addAll(elements: Collection<E>): Boolean = list.addAll(elements)

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean = list.addAll(index, elements)

    actual override fun clear() {
        list.clear()
    }

    actual override fun get(index: Int): E = list[index]

    actual override fun set(index: Int, element: E): E = list.set(index, element)

    actual override fun add(index: Int, element: E) {
        list.add(index, element)
    }

    actual override fun removeAt(index: Int): E = list.removeAt(index)

    actual override fun indexOf(element: E): Int = list.indexOf(element)

    actual override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    actual override fun peek(): E? = if (list.isEmpty()) null else list[0]

    actual override fun poll(): E? = if (list.isEmpty()) null else list.removeAt(0)

    actual override fun offer(e: E): Boolean = add(e)

    actual override fun offerFirst(e: E): Boolean {
        addFirst(e)
        return true
    }

    actual override fun offerLast(e: E): Boolean {
        addLast(e)
        return true
    }

    actual override fun peekFirst(): E? = peek()

    actual override fun peekLast(): E? = if (list.isEmpty()) null else list[list.size - 1]

    actual override fun pollFirst(): E? = poll()

    actual override fun pollLast(): E? = if (list.isEmpty()) null else list.removeAt(list.size - 1)

    actual override fun push(e: E) {
        addFirst(e)
    }

    actual override fun pop(): E = removeFirst()

    actual open fun removeFirstOccurrence(o: Any?): Boolean {
        val idx = list.indexOf(o)
        if (idx >= 0) {
            list.removeAt(idx)
            return true
        }
        return false
    }

    actual open fun removeLastOccurrence(o: Any?): Boolean {
        val idx = list.lastIndexOf(o)
        if (idx >= 0) {
            list.removeAt(idx)
            return true
        }
        return false
    }

    actual override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    actual override fun descendingIterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private var cursor = list.size - 1
            private var lastRet = -1

            override fun hasNext(): Boolean = cursor >= 0

            override fun next(): E {
                if (cursor < 0) throw NoSuchElementException()
                lastRet = cursor
                return list[cursor--]
            }

            override fun remove() {
                if (lastRet < 0) throw IllegalStateException()
                list.removeAt(lastRet)
                lastRet = -1
            }
        }
    }

    actual open fun clone(): Any = LinkedList(list)
}
