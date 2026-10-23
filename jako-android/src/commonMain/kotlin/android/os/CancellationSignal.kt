@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * [Nullability Decision] `setOnCancelListener` accepts nullable `OnCancelListener?` (passing `null` clears the listener),
 * matching AOSP `android.os.CancellationSignal`.
 */
expect class CancellationSignal() {
    val isCanceled: Boolean
    fun isCanceled(): Boolean
    fun throwIfCanceled()
    fun cancel()
    fun setOnCancelListener(listener: OnCancelListener?)

    fun interface OnCancelListener {
        fun onCancel()
    }
}
