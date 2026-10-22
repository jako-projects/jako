package java.util.concurrent

import kotlin.concurrent.withLock

actual abstract class CountedCompleter<T> : ForkJoinTask<T> {
    private val completerRef: CountedCompleter<*>?
    private var pendingCountValue: Int = 0

    protected actual constructor(completer: CountedCompleter<*>?, initialPendingCount: Int) : super() {
        this.completerRef = completer
        this.pendingCountValue = initialPendingCount
    }

    protected actual constructor(completer: CountedCompleter<*>?) : this(completer, 0)

    protected actual constructor() : this(null, 0)

    actual abstract fun compute()

    actual open fun onCompletion(caller: CountedCompleter<*>) {}

    actual open fun onExceptionalCompletion(ex: Throwable, caller: CountedCompleter<*>): Boolean = true

    actual fun getCompleter(): CountedCompleter<*>? = completerRef

    actual fun getPendingCount(): Int = taskLock.withLock { pendingCountValue }

    actual fun setPendingCount(count: Int) {
        taskLock.withLock { pendingCountValue = count }
    }

    actual fun addToPendingCount(delta: Int) {
        taskLock.withLock { pendingCountValue += delta }
    }

    actual fun compareAndSetPendingCount(expected: Int, count: Int): Boolean = taskLock.withLock {
        if (pendingCountValue == expected) {
            pendingCountValue = count
            true
        } else {
            false
        }
    }

    actual fun decrementPendingCountUnlessZero(): Int = taskLock.withLock {
        val c = pendingCountValue
        if (c != 0) {
            pendingCountValue = c - 1
        }
        c
    }

    actual fun getRoot(): CountedCompleter<*> {
        var a: CountedCompleter<*> = this
        while (true) {
            val p = a.completerRef ?: return a
            a = p
        }
    }

    actual fun tryComplete() {
        var a: CountedCompleter<*> = this
        var s: CountedCompleter<*> = a
        while (true) {
            val wasPositive = a.taskLock.withLock {
                if (a.pendingCountValue == 0) {
                    false
                } else {
                    a.pendingCountValue -= 1
                    true
                }
            }
            if (wasPositive) return
            a.onCompletion(s)
            s = a
            val p = a.completerRef
            if (p == null) {
                a.quietlyComplete()
                return
            }
            a = p
        }
    }

    actual fun propagateCompletion() {
        var a: CountedCompleter<*> = this
        while (true) {
            val wasPositive = a.taskLock.withLock {
                if (a.pendingCountValue == 0) {
                    false
                } else {
                    a.pendingCountValue -= 1
                    true
                }
            }
            if (wasPositive) return
            val p = a.completerRef
            if (p == null) {
                a.quietlyComplete()
                return
            }
            a = p
        }
    }

    actual override fun complete(rawResult: T) {
        setRawResult(rawResult)
        onCompletion(this)
        quietlyComplete()
        completerRef?.tryComplete()
    }

    actual fun firstComplete(): CountedCompleter<*>? {
        while (true) {
            val c = decrementPendingCountUnlessZero()
            if (c == 0) return this
            return null
        }
    }

    actual fun nextComplete(): CountedCompleter<*>? {
        val p = completerRef
        if (p != null) return p.firstComplete()
        quietlyComplete()
        return null
    }

    actual fun quietlyCompleteRoot() {
        getRoot().quietlyComplete()
    }

    actual fun helpComplete(maxTasks: Int) {
        if (maxTasks > 0 && status < 2) {
            val pool = currentThreadForkJoinPool() ?: submittedPool
            var remaining = maxTasks
            while (remaining > 0 && status < 2) {
                if (pool == null || !pool.tryHelpOneTask()) break
                remaining--
            }
        }
    }

    override fun recordExceptionalCompletion(ex: Throwable) {
        var a: CountedCompleter<*> = this
        var s: CountedCompleter<*> = a
        while (true) {
            val recorded = a.taskLock.withLock {
                if (a.status < 2) {
                    a.exception = ex
                    a.status = 4
                    a.doneCond.signalAll()
                    true
                } else {
                    false
                }
            }
            if (!recorded) break
            if (!a.onExceptionalCompletion(ex, s)) break
            s = a
            val p = a.completerRef ?: break
            a = p
        }
    }

    actual final override fun exec(): Boolean {
        compute()
        return false
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun getRawResult(): T = null as T

    actual override fun setRawResult(t: T) {}
}
