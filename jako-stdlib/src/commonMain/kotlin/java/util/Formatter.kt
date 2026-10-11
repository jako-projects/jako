@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Closeable
import java.io.Flushable

expect class Formatter : Closeable, Flushable {
    constructor()
    // [Nullability Decision] Java Formatter(Appendable) destination is non-null; null throws NPE.
    constructor(a: Appendable)
    constructor(l: Locale?)
    constructor(a: Appendable, l: Locale?)
    fun format(format: String, vararg args: Any?): Formatter
    fun format(l: Locale?, format: String, vararg args: Any?): Formatter
    fun locale(): Locale
    fun out(): Appendable
    override fun flush()
    override fun close()
    override fun toString(): String
}
