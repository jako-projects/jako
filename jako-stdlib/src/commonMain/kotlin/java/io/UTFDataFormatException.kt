@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class UTFDataFormatException : IOException {
    constructor()
    constructor(s: String?)
}
