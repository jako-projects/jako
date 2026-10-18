@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.Serializable

import kotlin.Long
import kotlin.Int

expect fun interface Runnable {
    fun run()
}

/**
 * [Nullability Decision] Java Readable.read(CharBuffer) takes a non-null CharBuffer.
 */
expect fun interface Readable {
    fun read(cb: java.nio.CharBuffer): Int
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

    enum class State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    fun interface UncaughtExceptionHandler {
        fun uncaughtException(t: Thread, e: Throwable)
    }

    open var name: String
    open var isDaemon: Boolean
    open var priority: Int
    open val id: Long
    open val isAlive: Boolean
    open val isInterrupted: Boolean
    open val state: State
    open var uncaughtExceptionHandler: UncaughtExceptionHandler?

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
    open fun getPriority(): Int
    open fun setPriority(newPriority: Int)
    open fun getState(): State
    open fun getUncaughtExceptionHandler(): UncaughtExceptionHandler?
    open fun setUncaughtExceptionHandler(eh: UncaughtExceptionHandler?)
    open fun join()
    open fun join(millis: Long)
    open fun join(millis: Long, nanos: Int)

    companion object {
        val MIN_PRIORITY: Int
        val NORM_PRIORITY: Int
        val MAX_PRIORITY: Int
        var defaultUncaughtExceptionHandler: UncaughtExceptionHandler?

        fun currentThread(): Thread
        fun sleep(millis: Long)
        fun sleep(millis: Long, nanos: Int)
        fun yield()
        fun interrupted(): Boolean
        fun holdsLock(obj: Any): Boolean
        fun getDefaultUncaughtExceptionHandler(): UncaughtExceptionHandler?
        fun setDefaultUncaughtExceptionHandler(eh: UncaughtExceptionHandler?)
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

expect open class InheritableThreadLocal<T> : ThreadLocal<T> {
    constructor()
    protected open fun childValue(parentValue: T?): T?
}

/**
 * [Nullability Decision] declaringClass and methodName are non-null (Java throws NPE if null);
 * fileName is nullable (null indicates Unknown Source).
 */
expect class StackTraceElement(
    declaringClass: String,
    methodName: String,
    fileName: String?,
    lineNumber: Int
) : Serializable {
    fun getClassName(): String
    fun getMethodName(): String
    fun getFileName(): String?
    fun getLineNumber(): Int
    fun isNativeMethod(): Boolean

    val className: String
    val methodName: String
    val fileName: String?
    val lineNumber: Int
    val isNativeMethod: Boolean

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
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
    fun getCanonicalName(): String?
    fun isPrimitive(): Boolean
    fun isArray(): Boolean
    fun isInstance(obj: Any?): Boolean
    fun getComponentType(): Class<*>?
    fun cast(obj: Any?): T?

    val name: String
    val simpleName: String
    val canonicalName: String?
    val isPrimitive: Boolean
    val isArray: Boolean
    val componentType: Class<*>?

    companion object {
        fun forName(className: String): Class<*>
    }
}

expect class Void {
    companion object {
        val TYPE: Class<Void>
    }
}

expect open class ClassNotFoundException : Exception {
    constructor()
    constructor(s: String?)
    constructor(s: String?, ex: Throwable?)
    open fun getException(): Throwable?
}

expect open class NoSuchFieldException : Exception {
    constructor()
    constructor(s: String?)
}

expect open class SecurityException : RuntimeException {
    constructor()
    constructor(s: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

expect open class IllegalMonitorStateException : RuntimeException {
    constructor()
    constructor(s: String?)
}

expect open class NegativeArraySizeException : RuntimeException {
    constructor()
    constructor(s: String?)
}

expect open class ArrayStoreException : RuntimeException {
    constructor()
    constructor(s: String?)
}

expect open class StringIndexOutOfBoundsException : IndexOutOfBoundsException {
    constructor()
    constructor(s: String?)
    constructor(index: Int)
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
