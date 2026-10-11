@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.InputStream
import java.io.PrintStream

expect object System {
    fun currentTimeMillis(): Long
    fun nanoTime(): Long
    fun identityHashCode(x: Any?): Int
    fun getProperty(key: String): String?
    fun getProperty(key: String, default: String): String
    fun arraycopy(src: Any, srcPos: Int, dest: Any, destPos: Int, length: Int)
    fun lineSeparator(): String
    fun getenv(name: String): String?

    /**
     * Standard input. Java allows [setIn] to install null; consumers treat it as non-null.
     * [Nullability Decision] Java `System.in` is a platform type; jako exposes non-null InputStream.
     */
    val `in`: InputStream
    val out: PrintStream
    val err: PrintStream

    /** [Nullability Decision] Java System.setIn(InputStream) accepts null. */
    fun setIn(inStream: InputStream?)
    /** [Nullability Decision] Java System.setOut(PrintStream) accepts null. */
    fun setOut(out: PrintStream?)
    /** [Nullability Decision] Java System.setErr(PrintStream) accepts null. */
    fun setErr(err: PrintStream?)
}
