package java.util

import java.io.Serializable
import java.lang.Cloneable

actual interface SortedSet<E> : MutableSet<E> {
    actual fun comparator(): Comparator<in E>?
    actual fun first(): E
    actual fun last(): E
}

actual interface NavigableSet<E> : SortedSet<E> {
    actual fun descendingIterator(): MutableIterator<E>
}

actual open class TreeSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    private val comp: Comparator<in E>?
    private val elements = ArrayList<E>()

    actual constructor() {
        this.comp = null
    }

    actual constructor(comparator: Comparator<in E>?) {
        this.comp = comparator
    }

    actual constructor(c: Collection<E>) : this() {
        addAll(c)
    }

    actual constructor(s: SortedSet<E>) : this(s.comparator()) {
        addAll(s)
    }

    private fun compareElements(a: E, b: E): Int {
        return if (comp != null) {
            comp.compare(a, b)
        } else {
            @Suppress("UNCHECKED_CAST")
            (a as Comparable<E>).compareTo(b)
        }
    }

    actual override val size: Int
        get() = elements.size

    actual override fun iterator(): MutableIterator<E> {
        val it = elements.iterator()
        return object : MutableIterator<E> {
            private var last: E? = null
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): E {
                val elem = it.next()
                last = elem
                return elem
            }
            override fun remove() {
                it.remove()
            }
        }
    }

    actual override fun descendingIterator(): MutableIterator<E> {
        val reversed = elements.reversed().toMutableList()
        val it = reversed.iterator()
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
                    this@TreeSet.remove(elem)
                }
            }
        }
    }

    actual override fun add(element: E): Boolean {
        for (i in elements.indices) {
            val cmp = compareElements(element, elements[i])
            if (cmp == 0) return false // Duplicate
            if (cmp < 0) {
                elements.add(i, element)
                return true
            }
        }
        elements.add(element)
        return true
    }

    actual override fun contains(element: E): Boolean {
        for (e in elements) {
            if (compareElements(element, e) == 0) return true
        }
        return false
    }

    actual override fun remove(element: E): Boolean {
        for (i in elements.indices) {
            if (compareElements(element, elements[i]) == 0) {
                elements.removeAt(i)
                return true
            }
        }
        return false
    }

    actual override fun clear() {
        elements.clear()
    }

    actual override fun comparator(): Comparator<in E>? = comp

    actual override fun first(): E {
        if (elements.isEmpty()) throw NoSuchElementException()
        return elements.first()
    }

    actual override fun last(): E {
        if (elements.isEmpty()) throw NoSuchElementException()
        return elements.last()
    }
}

