package java.util.concurrent

actual fun interface Callable<V> {
    actual fun call(): V
}
