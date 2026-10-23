@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * [Nullability Decision] `message` parameter is nullable (`String?`), matching AOSP `OperationCanceledException`.
 */
expect open class OperationCanceledException : RuntimeException {
    constructor()
    constructor(message: String?)
}
