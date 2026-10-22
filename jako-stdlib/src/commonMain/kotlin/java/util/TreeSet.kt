@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class TreeSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in E>?)
    constructor(c: Collection<E>)
    constructor(s: SortedSet<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun comparator(): Comparator<in E>?
    override fun first(): E
    override fun last(): E
}
