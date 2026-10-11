@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface Queue<E> : MutableCollection<E> {
    fun offer(e: E): Boolean
    fun poll(): E?
    fun peek(): E?
}

expect abstract class AbstractQueue<E> : AbstractMutableCollection<E>, Queue<E> {
    protected constructor()
    override fun add(element: E): Boolean
    override fun clear()
}

expect interface Deque<E> : Queue<E> {
    fun addFirst(e: E)
    fun addLast(e: E)
    fun offerFirst(e: E): Boolean
    fun offerLast(e: E): Boolean
    fun removeFirst(): E
    fun removeLast(): E
    fun pollFirst(): E?
    fun pollLast(): E?
    fun getFirst(): E
    fun getLast(): E
    fun peekFirst(): E?
    fun peekLast(): E?
    fun pop(): E
    fun push(e: E)
    fun descendingIterator(): MutableIterator<E>
}
