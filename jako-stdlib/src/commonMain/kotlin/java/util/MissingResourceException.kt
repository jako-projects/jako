@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class MissingResourceException : RuntimeException {
    constructor(s: String?, className: String?, key: String?)

    open fun getClassName(): String?
    open val className: String?
    open fun getKey(): String?
    open val key: String?
}
