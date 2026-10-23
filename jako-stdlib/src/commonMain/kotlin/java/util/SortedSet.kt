@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface SortedSet<E> : MutableSet<E> {
    fun comparator(): Comparator<in E>?
    fun first(): E
    fun last(): E
}
