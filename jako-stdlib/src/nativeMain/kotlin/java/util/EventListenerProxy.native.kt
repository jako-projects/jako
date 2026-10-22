package java.util

actual abstract class EventListenerProxy<T : EventListener> actual constructor(
    listener: T
) : EventListener {
    private val listenerVal: T = listener
    actual open fun getListener(): T = listenerVal
    actual open val listener: T
        get() = listenerVal
}
