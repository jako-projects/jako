@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

import java.lang.Runnable

expect open class CyclicBarrier {
    constructor(parties: Int, barrierAction: Runnable?)
    constructor(parties: Int)

    open fun await(): Int
    open fun await(timeout: Long, unit: TimeUnit): Int
    open fun getParties(): Int
    open fun isBroken(): Boolean
    open fun reset()
    open fun getNumberWaiting(): Int
    open val parties: Int
    open val isBroken: Boolean
    open val numberWaiting: Int
}
