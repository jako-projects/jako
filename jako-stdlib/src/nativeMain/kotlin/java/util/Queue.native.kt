package java.util

actual interface Queue<E> : MutableCollection<E> {
    actual fun offer(e: E): Boolean
    actual fun poll(): E?
    actual fun peek(): E?
}
