package java.util.concurrent

import java.io.Serializable
import java.lang.Cloneable

actual open class CopyOnWriteArrayList<E> : MutableList<E>, RandomAccess, Cloneable, Serializable {
    private var list = ArrayList<E>()

    actual constructor()
    actual constructor(c: Collection<E>) {
        list.addAll(c)
    }
    actual constructor(array: Array<E>) {
        list.addAll(array)
    }

    actual open fun addIfAbsent(element: E): Boolean {
        val newList = ArrayList(list)
        if (newList.contains(element)) return false
        newList.add(element)
        list = newList
        return true
    }

    actual open fun addAllAbsent(c: Collection<E>): Int {
        val newList = ArrayList(list)
        var added = 0
        for (e in c) {
            if (!newList.contains(e)) {
                newList.add(e)
                added++
            }
        }
        if (added > 0) list = newList
        return added
    }

    actual override val size: Int
        get() = list.size

    actual override fun isEmpty(): Boolean = list.isEmpty()

    actual override fun contains(element: E): Boolean = list.contains(element)

    actual override fun containsAll(elements: Collection<E>): Boolean = list.containsAll(elements)

    actual override fun get(index: Int): E = list[index]

    actual override fun indexOf(element: E): Int = list.indexOf(element)

    actual override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    actual override fun iterator(): MutableIterator<E> {
        val snapshot = ArrayList(list)
        return snapshot.iterator()
    }

    actual override fun listIterator(): MutableListIterator<E> {
        val snapshot = ArrayList(list)
        return snapshot.listIterator()
    }

    actual override fun listIterator(index: Int): MutableListIterator<E> {
        val snapshot = ArrayList(list)
        return snapshot.listIterator(index)
    }

    actual override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        val snapshot = ArrayList(list)
        return snapshot.subList(fromIndex, toIndex)
    }

    actual override fun add(element: E): Boolean {
        val newList = ArrayList(list)
        newList.add(element)
        list = newList
        return true
    }

    actual override fun add(index: Int, element: E) {
        val newList = ArrayList(list)
        newList.add(index, element)
        list = newList
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val newList = ArrayList(list)
        val res = newList.addAll(index, elements)
        if (res) list = newList
        return res
    }

    actual override fun addAll(elements: Collection<E>): Boolean {
        val newList = ArrayList(list)
        val res = newList.addAll(elements)
        if (res) list = newList
        return res
    }

    actual override fun clear() {
        list = ArrayList()
    }

    actual override fun remove(element: E): Boolean {
        val newList = ArrayList(list)
        val res = newList.remove(element)
        if (res) list = newList
        return res
    }

    actual override fun removeAll(elements: Collection<E>): Boolean {
        val newList = ArrayList(list)
        val res = newList.removeAll(elements)
        if (res) list = newList
        return res
    }

    actual override fun retainAll(elements: Collection<E>): Boolean {
        val newList = ArrayList(list)
        val res = newList.retainAll(elements)
        if (res) list = newList
        return res
    }

    actual override fun removeAt(index: Int): E {
        val newList = ArrayList(list)
        val removed = newList.removeAt(index)
        list = newList
        return removed
    }

    actual override fun set(index: Int, element: E): E {
        val newList = ArrayList(list)
        val old = newList.set(index, element)
        list = newList
        return old
    }
}
