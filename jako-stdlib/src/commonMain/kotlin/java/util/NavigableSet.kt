@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect interface NavigableSet<E> : SortedSet<E> {
    fun descendingIterator(): MutableIterator<E>
}
