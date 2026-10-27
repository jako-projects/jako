package java.nio.channels

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual abstract class SelectableChannel protected actual constructor() : Channel, InterruptibleChannel {
    private val regLock = ReentrantLock()
    private val blockLockObj = Any()
    private var blockingMode = true
    private val registrations = mutableListOf<SelectionKeyImpl>()

    actual abstract fun validOps(): Int

    internal open fun pollReadyOps(): Int = 0

    internal open fun nativeSocketHandle(): Long = -1L

    internal fun notifySelectors() {
        val keysSnapshot = regLock.withLock { registrations.toList() }
        for (key in keysSnapshot) {
            if (key.isValid()) {
                key.selectorImpl.signalChannelActivity()
            }
        }
    }

    internal fun closeSelectableChannel() {
        val keysSnapshot = regLock.withLock {
            val copy = registrations.toList()
            registrations.clear()
            copy
        }
        for (key in keysSnapshot) {
            key.cancel()
            key.selectorImpl.signalChannelActivity()
        }
    }

    internal fun removeKey(key: SelectionKeyImpl) {
        regLock.withLock {
            registrations.remove(key)
        }
    }

    actual open fun isRegistered(): Boolean = regLock.withLock {
        registrations.any { it.isValid() }
    }

    actual open fun keyFor(sel: Selector): SelectionKey? = regLock.withLock {
        registrations.firstOrNull { it.selector() === sel && it.isValid() }
    }

    actual open fun register(sel: Selector, ops: Int, att: Any?): SelectionKey {
        if (!isOpen()) throw ClosedChannelException()
        if (!sel.isOpen()) throw ClosedSelectorException()
        if (sel !is SelectorImpl) throw IllegalSelectorException()
        if ((ops and validOps().inv()) != 0) {
            throw IllegalArgumentException("Invalid interestOps: $ops (validOps=${validOps()})")
        }
        return regLock.withLock {
            if (blockingMode) throw IllegalBlockingModeException()
            if (!isOpen()) throw ClosedChannelException()
            val existing = registrations.firstOrNull { it.selector() === sel }
            if (existing != null) {
                if (!existing.isValid()) throw CancelledKeyException()
                existing.interestOps(ops)
                existing.attach(att)
                return@withLock existing
            }
            val key = SelectionKeyImpl(this, sel, ops, att)
            registrations.add(key)
            sel.registerKey(key)
            key
        }
    }

    actual fun register(sel: Selector, ops: Int): SelectionKey = register(sel, ops, null)

    actual open fun configureBlocking(block: Boolean): SelectableChannel {
        if (!isOpen()) throw ClosedChannelException()
        regLock.withLock {
            if (block && registrations.any { it.isValid() }) {
                throw IllegalBlockingModeException()
            }
            blockingMode = block
        }
        return this
    }

    actual open fun isBlocking(): Boolean = regLock.withLock { blockingMode }

    actual open fun blockingLock(): Any = blockLockObj
}

internal class SelectionKeyImpl(
    private val ch: SelectableChannel,
    internal val selectorImpl: SelectorImpl,
    initialInterestOps: Int,
    initialAttachment: Any?
) : SelectionKey() {
    private val lock = ReentrantLock()
    private var validFlag = true
    private var interest = initialInterestOps
    internal var ready = 0

    init {
        attach(initialAttachment)
    }

    override fun channel(): SelectableChannel = ch
    override fun selector(): Selector = selectorImpl

    override fun isValid(): Boolean = lock.withLock { validFlag && selectorImpl.isOpen() && ch.isOpen() }

    override fun cancel() {
        val shouldNotify = lock.withLock {
            if (!validFlag) return@withLock false
            validFlag = false
            true
        }
        if (shouldNotify) {
            selectorImpl.enqueueCancelledKey(this)
        }
    }

    override fun interestOps(): Int = lock.withLock {
        if (!isValid()) throw CancelledKeyException()
        interest
    }

    override fun interestOps(ops: Int): SelectionKey {
        if ((ops and ch.validOps().inv()) != 0) {
            throw IllegalArgumentException("Invalid ops: $ops")
        }
        lock.withLock {
            if (!isValid()) throw CancelledKeyException()
            interest = ops
        }
        selectorImpl.signalChannelActivity()
        return this
    }

    override fun readyOps(): Int = lock.withLock {
        if (!isValid()) throw CancelledKeyException()
        ready
    }

    internal fun setReadyOpsInternal(ops: Int) = lock.withLock {
        ready = ops
    }

    internal fun getReadyOpsInternal(): Int = lock.withLock { ready }
    internal fun getInterestOpsInternal(): Int = lock.withLock { interest }
    internal fun invalidateDirectly() = lock.withLock { validFlag = false }
}

internal expect class NativeSelectorMux() {
    fun await(handles: LongArray, interestOps: IntArray, count: Int, timeoutMs: Int): Boolean
    fun wakeup()
    fun clearWakeup()
    fun close()
}

internal class SelectorImpl : Selector() {
    private val lock = ReentrantLock()
    private val mux = NativeSelectorMux()
    private var openFlag = true
    private var wakeupFlag = false
    private val keySet = LinkedHashSet<SelectionKey>()
    private val selectedKeySet = LinkedHashSet<SelectionKey>()
    private val cancelledKeys = LinkedHashSet<SelectionKeyImpl>()

    internal fun signalChannelActivity() {
        mux.wakeup()
    }

    internal fun registerKey(key: SelectionKeyImpl) {
        lock.withLock {
            if (!openFlag) throw ClosedSelectorException()
            keySet.add(key)
            mux.wakeup()
        }
    }

    internal fun enqueueCancelledKey(key: SelectionKeyImpl) {
        lock.withLock {
            cancelledKeys.add(key)
            mux.wakeup()
        }
    }

    private fun processCancelledKeysLocked() {
        if (cancelledKeys.isEmpty()) return
        for (k in cancelledKeys) {
            keySet.remove(k)
            selectedKeySet.remove(k)
            k.channel().removeKey(k)
        }
        cancelledKeys.clear()
    }

    private fun doSelectLocked(): Int {
        processCancelledKeysLocked()
        var updatedCount = 0
        for (rawKey in keySet.toList()) {
            val key = rawKey as SelectionKeyImpl
            if (!key.isValid()) {
                cancelledKeys.add(key)
                continue
            }
            val interest = key.getInterestOpsInternal()
            if (interest == 0) continue
            val ready = key.channel().pollReadyOps() and interest
            if (ready != 0) {
                if (selectedKeySet.add(key)) {
                    key.setReadyOpsInternal(ready)
                    updatedCount++
                } else {
                    val prev = key.getReadyOpsInternal()
                    val merged = prev or ready
                    key.setReadyOpsInternal(merged)
                    if (merged != prev) {
                        updatedCount++
                    }
                }
            }
        }
        processCancelledKeysLocked()
        return updatedCount
    }

    override fun isOpen(): Boolean = lock.withLock { openFlag }

    override fun keys(): MutableSet<SelectionKey> = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        keySet
    }

    override fun selectedKeys(): MutableSet<SelectionKey> = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        selectedKeySet
    }

    override fun selectNow(): Int = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        wakeupFlag = false
        mux.clearWakeup()
        doSelectLocked()
    }

    override fun select(timeout: Long): Int {
        if (timeout < 0L) throw IllegalArgumentException("Negative timeout: $timeout")
        val deadline = if (timeout > 0L) java.lang.System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout) else 0L
        while (true) {
            var handles = LongArray(0)
            var interests = IntArray(0)
            var count = 0
            var waitMs = 0

            val immediateResult = lock.withLock {
                if (!openFlag) throw ClosedSelectorException()
                mux.clearWakeup()
                val n = doSelectLocked()
                if (n > 0 || wakeupFlag) {
                    wakeupFlag = false
                    return@withLock n
                }
                if (timeout > 0L) {
                    val remaining = deadline - java.lang.System.nanoTime()
                    if (remaining <= 0L) {
                        return@withLock 0
                    }
                    val remMs = (remaining + 999_999L) / 1_000_000L
                    waitMs = remMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                } else {
                    waitMs = -1
                }

                val maxCount = keySet.size
                handles = LongArray(maxCount)
                interests = IntArray(maxCount)
                var idx = 0
                for (rawKey in keySet) {
                    val key = rawKey as SelectionKeyImpl
                    if (!key.isValid()) continue
                    val interest = key.getInterestOpsInternal()
                    if (interest == 0) continue
                    val h = key.channel().nativeSocketHandle()
                    if (h != -1L) {
                        handles[idx] = h
                        interests[idx] = interest
                        idx++
                    }
                }
                count = idx
                -1
            }

            if (immediateResult >= 0) {
                return immediateResult
            }

            mux.await(handles, interests, count, waitMs)
        }
    }

    override fun select(): Int = select(0L)

    override fun wakeup(): Selector = lock.withLock {
        wakeupFlag = true
        mux.wakeup()
        this
    }

    override fun close() {
        val keysToClose = lock.withLock {
            if (!openFlag) return
            openFlag = false
            wakeupFlag = true
            val list = keySet.toList()
            keySet.clear()
            selectedKeySet.clear()
            cancelledKeys.clear()
            mux.wakeup()
            mux.close()
            list
        }
        for (k in keysToClose) {
            if (k is SelectionKeyImpl) {
                k.invalidateDirectly()
                k.channel().removeKey(k)
            }
        }
    }
}
