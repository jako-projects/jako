@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.io.Serializable

expect open class CopyOnWriteArraySet<E> : MutableSet<E>, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: E): Boolean
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun iterator(): MutableIterator<E>
    override fun containsAll(elements: Collection<E>): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
}
