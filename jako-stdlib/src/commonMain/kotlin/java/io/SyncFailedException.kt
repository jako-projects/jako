@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class SyncFailedException : IOException {
    constructor(message: String?)
}
