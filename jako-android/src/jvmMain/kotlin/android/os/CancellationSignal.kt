package android.os

class CancellationSignal {
    private var canceled = false
    private var onCancelListener: OnCancelListener? = null
    private var cancelInProgress = false
    private val lock = Object()

    val isCanceled: Boolean
        @JvmName("isCanceledProp")
        get() = isCanceled()

    fun isCanceled(): Boolean {
        synchronized(lock) {
            return canceled
        }
    }

    fun throwIfCanceled() {
        if (isCanceled()) {
            throw OperationCanceledException()
        }
    }

    fun cancel() {
        val listener: OnCancelListener?
        synchronized(lock) {
            if (canceled) {
                return
            }
            canceled = true
            cancelInProgress = true
            listener = onCancelListener
        }
        try {
            listener?.onCancel()
        } finally {
            synchronized(lock) {
                cancelInProgress = false
                lock.notifyAll()
            }
        }
    }

    fun setOnCancelListener(listener: OnCancelListener?) {
        synchronized(lock) {
            while (cancelInProgress) {
                try {
                    lock.wait()
                } catch (_: InterruptedException) {
                }
            }
            if (onCancelListener === listener) {
                return
            }
            onCancelListener = listener
            if (!canceled || listener == null) {
                return
            }
        }
        listener?.onCancel()
    }

    fun interface OnCancelListener {
        fun onCancel()
    }
}
