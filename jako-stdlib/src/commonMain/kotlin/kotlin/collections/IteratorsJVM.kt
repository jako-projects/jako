package kotlin.collections

import java.util.Enumeration

public operator fun <T> Enumeration<T>.iterator(): Iterator<T> = object : Iterator<T> {
    override fun hasNext(): Boolean = hasMoreElements()
    override fun next(): T = nextElement()
}
