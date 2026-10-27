package android.os

/**
 * [Nullability Decision] `message` parameter is nullable (`String?`), matching AOSP `OperationCanceledException`.
 */
actual open class OperationCanceledException : RuntimeException {
    actual constructor() : super("The operation has been canceled.")
    actual constructor(message: String?) : super(message ?: "The operation has been canceled.")
}
