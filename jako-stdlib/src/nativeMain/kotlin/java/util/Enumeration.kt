package java.util

actual interface Enumeration<E> {
    actual fun hasMoreElements(): Boolean
    actual fun nextElement(): E
}
