@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect abstract class EventListenerProxy<T : EventListener>(listener: T) : EventListener {
    open fun getListener(): T
    open val listener: T
}
