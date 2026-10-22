@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class ClassNotFoundException : Exception {
    constructor()
    constructor(s: String?)
    constructor(s: String?, ex: Throwable?)
    open fun getException(): Throwable?
}
