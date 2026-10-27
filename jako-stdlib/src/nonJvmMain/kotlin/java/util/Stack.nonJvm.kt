package java.util

actual open class Stack<E> actual constructor() : Vector<E>() {
    actual open fun push(item: E): E {
        addElement(item)
        return item
    }

    actual open fun pop(): E {
        val len = size
        val obj = peek()
        removeElementAt(len - 1)
        return obj
    }

    actual open fun peek(): E {
        val len = size
        if (len == 0) throw EmptyStackException()
        return elementAt(len - 1)
    }

    actual open fun empty(): Boolean = size == 0

    actual open fun search(o: Any?): Int {
        val i = lastIndexOf(o, size - 1)
        return if (i >= 0) size - i else -1
    }
}
