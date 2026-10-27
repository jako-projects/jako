package android.util

import com.bennyhuo.jako.JakoApplication
import java.net.UnknownHostException
import kotlin.math.abs

actual class Log private constructor() {
    actual companion object {
        actual val VERBOSE: Int = 2
        actual val DEBUG: Int = 3
        actual val INFO: Int = 4
        actual val WARN: Int = 5
        actual val ERROR: Int = 6
        actual val ASSERT: Int = 7

        actual fun v(tag: String?, msg: String): Int = println(VERBOSE, tag, msg)

        actual fun v(tag: String?, msg: String?, tr: Throwable?): Int =
            println(VERBOSE, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun d(tag: String?, msg: String): Int = println(DEBUG, tag, msg)

        actual fun d(tag: String?, msg: String?, tr: Throwable?): Int =
            println(DEBUG, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun i(tag: String?, msg: String): Int = println(INFO, tag, msg)

        actual fun i(tag: String?, msg: String?, tr: Throwable?): Int =
            println(INFO, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun w(tag: String?, msg: String): Int = println(WARN, tag, msg)

        actual fun w(tag: String?, msg: String?, tr: Throwable?): Int =
            println(WARN, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun w(tag: String?, tr: Throwable?): Int =
            println(WARN, tag, getStackTraceString(tr))

        actual fun e(tag: String?, msg: String): Int = println(ERROR, tag, msg)

        actual fun e(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ERROR, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun wtf(tag: String?, msg: String?): Int = wtf(tag, msg, null)

        actual fun wtf(tag: String?, tr: Throwable): Int = wtf(tag, tr.message, tr)

        actual fun wtf(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ASSERT, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        actual fun getStackTraceString(tr: Throwable?): String {
            if (tr == null) return ""
            var t: Throwable? = tr
            while (t != null) {
                if (t is UnknownHostException) {
                    return ""
                }
                t = t.cause
            }
            return tr.stackTraceToString()
        }

        actual fun println(priority: Int, tag: String?, msg: String): Int {
            val levelChar = when (priority) {
                VERBOSE -> 'V'
                DEBUG -> 'D'
                INFO -> 'I'
                WARN -> 'W'
                ERROR -> 'E'
                ASSERT -> 'A'
                else -> '?'
            }
            val formatted = "$levelChar/${tag ?: ""}: $msg"
            kotlin.io.println(formatted)
            return formatted.encodeToByteArray().size
        }

        actual fun isLoggable(tag: String?, level: Int): Boolean {
            return level >= JakoApplication.config.logMinPriority
        }
    }
}
