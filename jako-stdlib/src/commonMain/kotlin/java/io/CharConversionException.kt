@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class CharConversionException : IOException {
    constructor()
    constructor(s: String?)
}
