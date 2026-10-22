@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class Stack<E>() : Vector<E> {
    open fun push(item: E): E
    open fun pop(): E
    open fun peek(): E
    open fun empty(): Boolean
    open fun search(o: Any?): Int
}
