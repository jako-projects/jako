package android.os

open class OperationCanceledException : RuntimeException {
    constructor() : super("The operation has been canceled.")
    constructor(message: String?) : super(message ?: "The operation has been canceled.")
}
