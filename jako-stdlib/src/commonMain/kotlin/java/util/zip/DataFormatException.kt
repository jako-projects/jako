@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.zip

expect open class DataFormatException : Exception {
    constructor()
    constructor(s: String?)
}
