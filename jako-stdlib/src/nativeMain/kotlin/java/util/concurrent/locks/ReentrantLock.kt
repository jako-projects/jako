package java.util.concurrent.locks

import java.io.Serializable
import java.util.concurrent.TimeUnit

internal interface ExtendedLock : Lock {
    fun getHoldCount(): Int
    fun isHeldByCurrentThread(): Boolean
    fun isLocked(): Boolean
}

internal expect fun createPlatformLock(fair: Boolean): Lock

actual open class ReentrantLock actual constructor(
    private val fair: Boolean
) : Lock, Serializable {
    actual constructor() : this(false)

    private val delegate: Lock = createPlatformLock(fair)

    actual override fun lock() = delegate.lock()
    actual override fun lockInterruptibly() = delegate.lockInterruptibly()
    actual override fun tryLock(): Boolean = delegate.tryLock()
    actual override fun tryLock(time: Long, unit: TimeUnit): Boolean = delegate.tryLock(time, unit)
    actual override fun unlock() = delegate.unlock()
    actual override fun newCondition(): Condition = delegate.newCondition()

    actual open fun getHoldCount(): Int = (delegate as? ExtendedLock)?.getHoldCount() ?: 0
    actual open fun isHeldByCurrentThread(): Boolean = (delegate as? ExtendedLock)?.isHeldByCurrentThread() ?: false
    actual open fun isLocked(): Boolean = (delegate as? ExtendedLock)?.isLocked() ?: false
    actual fun isFair(): Boolean = fair

    actual open val holdCount: Int get() = getHoldCount()
    actual open val isHeldByCurrentThread: Boolean get() = isHeldByCurrentThread()
    actual open val isLocked: Boolean get() = isLocked()
    actual val isFair: Boolean get() = isFair()
}
