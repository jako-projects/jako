@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin

expect inline fun <R> synchronized(lock: Any, block: () -> R): R

