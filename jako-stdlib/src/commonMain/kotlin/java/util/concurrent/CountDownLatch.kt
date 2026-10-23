@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class CountDownLatch {
    constructor(count: Int)
    open fun await()
    open fun await(timeout: Long, unit: TimeUnit): Boolean
    open fun countDown()
    open fun getCount(): Long
}
