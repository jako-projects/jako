@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect fun interface Observer {
    fun update(o: Observable, arg: Any?)
}
