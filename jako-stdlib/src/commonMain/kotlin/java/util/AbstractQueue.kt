@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect abstract class AbstractQueue<E> : AbstractMutableCollection<E>, Queue<E> {
    protected constructor()
    override fun add(element: E): Boolean
    override fun clear()
}
