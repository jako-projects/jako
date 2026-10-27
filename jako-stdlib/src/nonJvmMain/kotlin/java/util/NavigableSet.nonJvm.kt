package java.util

actual interface NavigableSet<E> : SortedSet<E> {
    actual fun descendingIterator(): MutableIterator<E>
}
