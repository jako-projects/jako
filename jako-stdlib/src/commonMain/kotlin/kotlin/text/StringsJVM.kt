@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin.text

import java.nio.charset.Charset
import java.util.Formatter
import java.util.Locale

public expect fun String(codePoints: IntArray, offset: Int, count: Int): String
public expect fun String(bytes: ByteArray, offset: Int, length: Int, charset: Charset): String
public expect fun String(bytes: ByteArray, charset: Charset): String

public expect fun String.toByteArray(charset: Charset = Charsets.UTF_8): ByteArray

public fun String.format(vararg args: Any?): String {
    return Formatter().format(this, *args).toString()
}

public fun String.format(locale: Locale?, vararg args: Any?): String {
    return Formatter(locale).format(locale, this, *args).toString()
}

public fun String.Companion.format(format: String, vararg args: Any?): String {
    return Formatter().format(format, *args).toString()
}

public fun String.Companion.format(locale: Locale?, format: String, vararg args: Any?): String {
    return Formatter(locale).format(locale, format, *args).toString()
}

public fun String.lowercase(locale: Locale): String = this.lowercase()
public fun String.uppercase(locale: Locale): String = this.uppercase()
