@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect interface ScheduledFuture<V> : Delayed, Future<V>
