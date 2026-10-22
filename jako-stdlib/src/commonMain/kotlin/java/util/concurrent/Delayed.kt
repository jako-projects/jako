@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect interface Delayed : Comparable<Delayed> {
    fun getDelay(unit: TimeUnit): Long
}
