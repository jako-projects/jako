package java.util

actual interface Deque<E> : Queue<E> {
    actual fun addFirst(e: E)
    actual fun addLast(e: E)
    actual fun offerFirst(e: E): Boolean
    actual fun offerLast(e: E): Boolean
    actual fun removeFirst(): E
    actual fun removeLast(): E
    actual fun pollFirst(): E?
    actual fun pollLast(): E?
    actual fun getFirst(): E
    actual fun getLast(): E
    actual fun peekFirst(): E?
    actual fun peekLast(): E?
    actual fun pop(): E
    actual fun push(e: E)
    actual fun descendingIterator(): MutableIterator<E>
}
