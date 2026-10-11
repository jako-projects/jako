package kotlin.collections

import java.util.Enumeration

public fun <E> Enumeration<E>.iterator(): Iterator<E> = object : Iterator<E> {
    override fun hasNext(): Boolean = hasMoreElements()
    override fun next(): E = nextElement()
}

public fun <E> Enumeration<E>.toList(): List<E> {
    val list = ArrayList<E>()
    while (hasMoreElements()) {
        list.add(nextElement())
    }
    return list
}

public fun <E> Enumeration<E>.asSequence(): Sequence<E> = Sequence { iterator() }
