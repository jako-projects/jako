@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect fun interface Runnable {
    fun run()
}
