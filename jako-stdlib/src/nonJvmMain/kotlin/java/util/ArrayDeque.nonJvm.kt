package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class ArrayDeque<E> : AbstractQueue<E>, Deque<E>, Cloneable, Serializable {
    private val elements = mutableListOf<E>()

    actual constructor() : super()
    actual constructor(numElements: Int) : super()
    actual constructor(c: Collection<E>) : super() {
        elements.addAll(c)
    }

    actual override val size: Int get() = elements.size
    actual open val isEmpty: Boolean get() = elements.isEmpty()
    actual override fun isEmpty(): Boolean = elements.isEmpty()
    actual override fun contains(element: E): Boolean = elements.contains(element)
    actual override fun remove(element: E): Boolean = elements.remove(element)
    actual override fun clear() { elements.clear() }
    actual override fun iterator(): MutableIterator<E> = elements.iterator()

    actual override fun offer(e: E): Boolean = offerLast(e)
    actual override fun poll(): E? = pollFirst()
    actual override fun peek(): E? = peekFirst()

    actual override fun addFirst(e: E) { elements.add(0, e) }
    actual override fun addLast(e: E) { elements.add(e) }
    actual override fun offerFirst(e: E): Boolean { addFirst(e); return true }
    actual override fun offerLast(e: E): Boolean { addLast(e); return true }

    actual override fun removeFirst(): E = if (elements.isNotEmpty()) elements.removeAt(0) else throw NoSuchElementException()
    actual override fun removeLast(): E = if (elements.isNotEmpty()) elements.removeAt(elements.size - 1) else throw NoSuchElementException()

    actual override fun pollFirst(): E? = if (elements.isNotEmpty()) elements.removeAt(0) else null
    actual override fun pollLast(): E? = if (elements.isNotEmpty()) elements.removeAt(elements.size - 1) else null

    actual override fun getFirst(): E = elements.firstOrNull() ?: throw NoSuchElementException()
    actual override fun getLast(): E = elements.lastOrNull() ?: throw NoSuchElementException()

    actual override fun peekFirst(): E? = elements.firstOrNull()
    actual override fun peekLast(): E? = elements.lastOrNull()

    actual override fun pop(): E = removeFirst()
    actual override fun push(e: E) = addFirst(e)

    actual override fun descendingIterator(): MutableIterator<E> = elements.asReversed().iterator()

    actual open fun clone(): ArrayDeque<E> {
        val result = ArrayDeque<E>()
        result.elements.addAll(this.elements)
        return result
    }
}
