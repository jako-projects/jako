package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class Vector<E> : AbstractMutableList<E>, MutableList<E>, RandomAccess, Cloneable, Serializable {
    private val list: ArrayList<E>
    private var cap: Int
    private val capIncrement: Int

    actual constructor(initialCapacity: Int, capacityIncrement: Int) : super() {
        if (initialCapacity < 0) throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        this.list = ArrayList(initialCapacity)
        this.cap = initialCapacity
        this.capIncrement = capacityIncrement
    }

    actual constructor(initialCapacity: Int) : this(initialCapacity, 0)

    actual constructor() : this(10, 0)

    actual constructor(c: Collection<E>) : super() {
        this.list = ArrayList(c)
        this.cap = c.size
        this.capIncrement = 0
    }

    private fun growCapacityIfNeeded(minCap: Int) {
        if (minCap > cap) {
            val next = if (capIncrement > 0) cap + capIncrement else if (cap == 0) 10 else cap * 2
            cap = maxOf(minCap, next)
        }
    }

    actual open fun copyInto(anArray: Array<Any?>) {
        for (i in 0 until list.size) {
            anArray[i] = list[i]
        }
    }

    actual open fun trimToSize() {
        list.trimToSize()
        cap = list.size
    }

    actual open fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > 0) {
            list.ensureCapacity(minCapacity)
            growCapacityIfNeeded(minCapacity)
        }
    }

    actual open fun setSize(newSize: Int) {
        if (newSize < 0) throw IndexOutOfBoundsException("newSize < 0: $newSize")
        if (newSize > list.size) {
            growCapacityIfNeeded(newSize)
            @Suppress("UNCHECKED_CAST")
            while (list.size < newSize) {
                list.add(null as E)
            }
        } else {
            while (list.size > newSize) {
                list.removeAt(list.size - 1)
            }
        }
    }

    actual open fun capacity(): Int = maxOf(cap, list.size)

    actual override val size: Int
        get() = list.size

    actual override fun isEmpty(): Boolean = list.isEmpty()

    actual open fun elements(): Enumeration<E> {
        return object : Enumeration<E> {
            var count = 0
            override fun hasMoreElements(): Boolean = count < list.size
            override fun nextElement(): E {
                if (count < list.size) {
                    return list[count++]
                }
                throw NoSuchElementException("Vector Enumeration")
            }
        }
    }

    actual override fun contains(element: E): Boolean = list.contains(element)

    actual override fun indexOf(element: E): Int = list.indexOf(element)

    actual open fun indexOf(o: Any?, index: Int): Int {
        if (index < 0) throw IndexOutOfBoundsException("$index < 0")
        for (i in index until list.size) {
            if (list[i] == o) return i
        }
        return -1
    }

    actual override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    actual open fun lastIndexOf(o: Any?, index: Int): Int {
        if (index >= list.size) throw IndexOutOfBoundsException("$index >= ${list.size}")
        for (i in index downTo 0) {
            if (list[i] == o) return i
        }
        return -1
    }

    actual open fun elementAt(index: Int): E = list[index]

    actual open fun firstElement(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[0]
    }

    actual open fun lastElement(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[list.size - 1]
    }

    actual open fun setElementAt(obj: E, index: Int) {
        list[index] = obj
    }

    actual open fun removeElementAt(index: Int) {
        list.removeAt(index)
    }

    actual open fun insertElementAt(obj: E, index: Int) {
        growCapacityIfNeeded(list.size + 1)
        list.add(index, obj)
    }

    actual open fun addElement(obj: E) {
        growCapacityIfNeeded(list.size + 1)
        list.add(obj)
    }

    actual open fun removeElement(obj: Any?): Boolean {
        val idx = indexOf(obj, 0)
        if (idx >= 0) {
            removeElementAt(idx)
            return true
        }
        return false
    }

    actual open fun removeAllElements() {
        list.clear()
    }

    actual open fun clone(): Any {
        val copy = Vector<E>(capacity(), capIncrement)
        copy.list.addAll(this.list)
        return copy
    }

    actual override fun get(index: Int): E = list[index]

    actual override fun set(index: Int, element: E): E = list.set(index, element)

    actual override fun add(element: E): Boolean {
        growCapacityIfNeeded(list.size + 1)
        return list.add(element)
    }

    actual override fun remove(element: E): Boolean = list.remove(element)

    actual override fun add(index: Int, element: E) {
        growCapacityIfNeeded(list.size + 1)
        list.add(index, element)
    }

    actual override fun removeAt(index: Int): E = list.removeAt(index)

    actual override fun clear() {
        list.clear()
    }

    actual override fun addAll(elements: Collection<E>): Boolean {
        growCapacityIfNeeded(list.size + elements.size)
        return list.addAll(elements)
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        growCapacityIfNeeded(list.size + elements.size)
        return list.addAll(index, elements)
    }

    actual override fun iterator(): MutableIterator<E> = list.iterator()

    actual override fun listIterator(): MutableListIterator<E> = list.listIterator()

    actual override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    actual override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.subList(fromIndex, toIndex)
}
