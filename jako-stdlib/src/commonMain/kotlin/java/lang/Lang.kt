@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.Serializable

import kotlin.Long
import kotlin.Int

expect fun interface Runnable {
    fun run()
}

expect open class Object {
    fun wait()
    fun wait(timeoutMillis: Long)
    fun wait(timeoutMillis: Long, nanos: Int)
    fun notify()
    fun notifyAll()
}

expect open class InterruptedException : Exception {
    constructor()
    constructor(message: String?)
}

expect open class IllegalThreadStateException : IllegalArgumentException {
    constructor()
    constructor(s: String?)
}

expect open class Thread : Runnable {
    constructor()
    constructor(target: Runnable?)
    constructor(name: String)
    constructor(target: Runnable?, name: String)

    open var name: String
    open var isDaemon: Boolean
    open val id: Long
    open val isAlive: Boolean
    open val isInterrupted: Boolean

    open fun start()
    override fun run()
    open fun interrupt()
    open fun isInterrupted(): Boolean
    open fun isAlive(): Boolean
    open fun getName(): String
    open fun setName(name: String)
    open fun getId(): Long
    open fun isDaemon(): Boolean
    open fun setDaemon(on: Boolean)
    open fun join()
    open fun join(millis: Long)
    open fun join(millis: Long, nanos: Int)

    companion object {
        fun currentThread(): Thread
        fun sleep(millis: Long)
        fun sleep(millis: Long, nanos: Int)
        fun yield()
        fun interrupted(): Boolean
    }
}

expect open class ThreadLocal<T> {
    constructor()
    open fun get(): T
    open fun set(value: T?)
    open fun remove()
    protected open fun initialValue(): T?

    companion object {
        fun <S> withInitial(supplier: () -> S): ThreadLocal<S>
    }
}

expect object Character {
    fun isISOControl(codePoint: Int): Boolean
    fun isISOControl(ch: Char): Boolean
    fun isDigit(ch: Char): Boolean
    fun isDigit(codePoint: Int): Boolean
    fun isLetter(ch: Char): Boolean
    fun isLetter(codePoint: Int): Boolean
    fun isLetterOrDigit(ch: Char): Boolean
    fun isLetterOrDigit(codePoint: Int): Boolean
    fun isWhitespace(ch: Char): Boolean
    fun isWhitespace(codePoint: Int): Boolean
    fun toLowerCase(ch: Char): Char
    fun toLowerCase(codePoint: Int): Int
    fun toUpperCase(ch: Char): Char
    fun toUpperCase(codePoint: Int): Int
    fun digit(ch: Char, radix: Int): Int
    fun digit(codePoint: Int, radix: Int): Int
    fun forDigit(digit: Int, radix: Int): Char
    fun charCount(codePoint: Int): Int
    fun toChars(codePoint: Int): CharArray
    fun codePointAt(seq: CharSequence, index: Int): Int
}

@Suppress("UPPER_BOUND_CANNOT_BE_ARRAY")
expect class Class<T> : Serializable {
    fun getName(): String
    fun getSimpleName(): String
    val name: String
    val simpleName: String
    fun <U> cast(obj: Any?): U?

    companion object {
        fun forName(className: String): Class<*>
    }
}

expect open class ClassNotFoundException : Exception {
    constructor()
    constructor(s: String?)
    constructor(s: String?, ex: Throwable?)
}

expect open class NoSuchFieldException : Exception {
    constructor()
    constructor(s: String?)
}

expect open class NoSuchMethodException : Exception {
    constructor()
    constructor(s: String?)
}

expect object Integer {
    val MAX_VALUE: Int
    val MIN_VALUE: Int
    fun parseInt(s: String): Int
    fun parseInt(s: String, radix: Int): Int
    fun toHexString(i: Int): String
    fun toBinaryString(i: Int): String
    fun compare(x: Int, y: Int): Int
    fun numberOfLeadingZeros(i: Int): Int
    fun valueOf(i: Int): Int
    fun valueOf(s: String): Int
    fun valueOf(s: String, radix: Int): Int
}

expect object Math {
    val PI: Double
    val E: Double
    fun min(a: Int, b: Int): Int
    fun min(a: Long, b: Long): Long
    fun min(a: Float, b: Float): Float
    fun min(a: Double, b: Double): Double
    fun max(a: Int, b: Int): Int
    fun max(a: Long, b: Long): Long
    fun max(a: Float, b: Float): Float
    fun max(a: Double, b: Double): Double
    fun abs(a: Int): Int
    fun abs(a: Long): Long
    fun abs(a: Float): Float
    fun abs(a: Double): Double
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
expect annotation class SafeVarargs()

