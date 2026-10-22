@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class ArrayStoreException : RuntimeException {
    constructor()
    constructor(s: String?)
}
