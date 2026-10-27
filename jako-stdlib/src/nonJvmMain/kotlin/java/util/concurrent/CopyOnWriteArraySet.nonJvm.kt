package java.util.concurrent

import java.io.Serializable

actual open class CopyOnWriteArraySet<E> : MutableSet<E>, Serializable {
    private val al: CopyOnWriteArrayList<E>

    actual constructor() {
        al = CopyOnWriteArrayList()
    }

    actual constructor(c: Collection<E>) {
        al = CopyOnWriteArrayList()
        al.addAllAbsent(c)
    }

    actual override val size: Int get() = al.size
    actual override fun isEmpty(): Boolean = al.isEmpty()
    actual override fun contains(element: E): Boolean = al.contains(element)
    actual override fun add(element: E): Boolean = al.addIfAbsent(element)
    actual override fun remove(element: E): Boolean = al.remove(element)
    actual override fun clear() = al.clear()
    actual override fun iterator(): MutableIterator<E> = al.iterator()
    actual override fun containsAll(elements: Collection<E>): Boolean = al.containsAll(elements)
    actual override fun addAll(elements: Collection<E>): Boolean = al.addAllAbsent(elements) > 0
    actual override fun removeAll(elements: Collection<E>): Boolean = al.removeAll(elements)
    actual override fun retainAll(elements: Collection<E>): Boolean = al.retainAll(elements)
}
