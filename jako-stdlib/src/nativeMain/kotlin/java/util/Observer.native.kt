package java.util

actual fun interface Observer {
    actual fun update(o: Observable, arg: Any?)
}
