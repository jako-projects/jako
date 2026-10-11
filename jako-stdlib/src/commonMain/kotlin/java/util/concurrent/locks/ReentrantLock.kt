@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.io.Serializable
import java.util.concurrent.TimeUnit

expect open class ReentrantLock : Lock, Serializable {
    constructor()
    constructor(fair: Boolean)
    override fun lock()
    override fun lockInterruptibly()
    override fun tryLock(): Boolean
    override fun tryLock(time: Long, unit: TimeUnit): Boolean
    override fun unlock()
    override fun newCondition(): Condition
    open fun getHoldCount(): Int
    open fun isHeldByCurrentThread(): Boolean
    open fun isLocked(): Boolean
    fun isFair(): Boolean

    open val holdCount: Int
    open val isHeldByCurrentThread: Boolean
    open val isLocked: Boolean
    val isFair: Boolean
}
