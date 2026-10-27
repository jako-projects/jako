package java.util

actual interface SortedSet<E> : MutableSet<E> {
    actual fun comparator(): Comparator<in E>?
    actual fun first(): E
    actual fun last(): E
}
