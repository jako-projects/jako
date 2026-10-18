@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin

import java.nio.charset.Charset
import java.util.Formatter
import java.util.Locale

public typealias System = java.lang.System
public typealias Integer = java.lang.Integer
public typealias Math = java.lang.Math
public typealias Class<T> = java.lang.Class<T>
public typealias Runnable = java.lang.Runnable
public typealias InterruptedException = java.lang.InterruptedException
public typealias Thread = java.lang.Thread
public typealias Object = java.lang.Object
public typealias Cloneable = java.lang.Cloneable
public typealias Character = java.lang.Character
public typealias SafeVarargs = java.lang.SafeVarargs
public typealias NoSuchFieldException = java.lang.NoSuchFieldException
public typealias IllegalMonitorStateException = java.lang.IllegalMonitorStateException
public typealias MalformedURLException = java.net.MalformedURLException
public typealias URISyntaxException = java.net.URISyntaxException
public typealias SuppressWarnings = Suppress
public typealias JvmField = kotlin.jvm.JvmField
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
public annotation class JvmName(val name: String)
public typealias JvmOverloads = kotlin.jvm.JvmOverloads
public typealias JvmStatic = kotlin.jvm.JvmStatic
public typealias Synchronized = kotlin.jvm.Synchronized
public typealias JvmSuppressWildcards = kotlin.jvm.JvmSuppressWildcards

public expect fun String(codePoints: IntArray, offset: Int, count: Int): String
public expect fun String(bytes: ByteArray, offset: Int, length: Int, charset: Charset): String
public expect fun String(bytes: ByteArray, charset: Charset): String

public fun String.Companion.format(format: String, vararg args: Any?): String {
    return Formatter().format(format, *args).toString()
}

public fun String.Companion.format(locale: Locale?, format: String, vararg args: Any?): String {
    return Formatter(locale).format(locale, format, *args).toString()
}

public fun Throwable.initCause(cause: Throwable?): Throwable = this
public val Throwable.suppressed: Array<Throwable> get() = suppressedExceptions.toTypedArray()
public fun Throwable.getSuppressed(): Array<Throwable> = suppressedExceptions.toTypedArray()

public expect val <T : Any> kotlin.reflect.KClass<T>.java: Class<T>
public expect val <T : Any> kotlin.reflect.KClass<T>.javaObjectType: Class<T>
public expect val <T : Any> kotlin.reflect.KClass<T>.javaPrimitiveType: Class<T>?
public expect val <T : Any> Class<T>.kotlin: kotlin.reflect.KClass<T>
public expect val <T : Any> T.javaClass: Class<T>
