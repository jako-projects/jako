package java.util

actual interface Queue<E> : MutableCollection<E> {
    actual fun offer(e: E): Boolean
    actual fun poll(): E?
    actual fun peek(): E?
}

actual abstract class AbstractQueue<E> protected actual constructor() : AbstractMutableCollection<E>(), Queue<E> {
    actual override fun add(element: E): Boolean {
        if (offer(element)) return true
        throw IllegalStateException("Queue full")
    }

    open fun remove(): E {
        return poll() ?: throw NoSuchElementException()
    }

    open fun element(): E {
        return peek() ?: throw NoSuchElementException()
    }

    actual override fun clear() {
        while (poll() != null) {}
    }
}
