package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * [Nullability Decision] `setOnCancelListener` accepts nullable `OnCancelListener?` (passing `null` clears the listener),
 * matching AOSP `android.os.CancellationSignal`.
 */
actual class CancellationSignal actual constructor() {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var canceled = false
    private var onCancelListener: OnCancelListener? = null
    private var cancelInProgress = false

    actual val isCanceled: Boolean
        get() = isCanceled()

    actual fun isCanceled(): Boolean = lock.withLock {
        canceled
    }

    actual fun throwIfCanceled() {
        if (isCanceled()) {
            throw OperationCanceledException()
        }
    }

    actual fun cancel() {
        val listener: OnCancelListener? = lock.withLock {
            if (canceled) {
                return
            }
            canceled = true
            cancelInProgress = true
            onCancelListener
        }
        try {
            listener?.onCancel()
        } finally {
            lock.withLock {
                cancelInProgress = false
                condition.signalAll()
            }
        }
    }

    actual fun setOnCancelListener(listener: OnCancelListener?) {
        val shouldInvoke = lock.withLock {
            while (cancelInProgress) {
                try {
                    condition.await()
                } catch (_: InterruptedException) {
                }
            }
            if (onCancelListener === listener) {
                false
            } else {
                onCancelListener = listener
                canceled && listener != null
            }
        }
        if (shouldInvoke) {
            listener?.onCancel()
        }
    }

    actual fun interface OnCancelListener {
        actual fun onCancel()
    }
}
