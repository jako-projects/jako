@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class UncheckedIOException : RuntimeException {
    constructor(message: String?, cause: IOException)
    constructor(cause: IOException)
    override val cause: IOException?
}
