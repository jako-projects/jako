@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect abstract class AbstractSequentialList<E> : AbstractMutableList<E> {
    protected constructor()

    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun iterator(): MutableIterator<E>
    abstract override fun listIterator(index: Int): MutableListIterator<E>
}
