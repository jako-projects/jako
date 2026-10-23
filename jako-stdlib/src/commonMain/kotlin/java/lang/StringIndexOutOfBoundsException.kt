@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Int

expect open class StringIndexOutOfBoundsException : IndexOutOfBoundsException {
    constructor()
    constructor(s: String?)
    constructor(index: Int)
}
