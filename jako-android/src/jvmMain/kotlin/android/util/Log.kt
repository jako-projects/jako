package android.util

import com.bennyhuo.jako.JakoApplication
import java.net.UnknownHostException

class Log private constructor() {
    companion object {
        const val VERBOSE: Int = 2
        const val DEBUG: Int = 3
        const val INFO: Int = 4
        const val WARN: Int = 5
        const val ERROR: Int = 6
        const val ASSERT: Int = 7

        @JvmStatic
        fun v(tag: String?, msg: String): Int = println(VERBOSE, tag, msg)

        @JvmStatic
        fun v(tag: String?, msg: String?, tr: Throwable?): Int =
            println(VERBOSE, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun d(tag: String?, msg: String): Int = println(DEBUG, tag, msg)

        @JvmStatic
        fun d(tag: String?, msg: String?, tr: Throwable?): Int =
            println(DEBUG, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun i(tag: String?, msg: String): Int = println(INFO, tag, msg)

        @JvmStatic
        fun i(tag: String?, msg: String?, tr: Throwable?): Int =
            println(INFO, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun w(tag: String?, msg: String): Int = println(WARN, tag, msg)

        @JvmStatic
        fun w(tag: String?, msg: String?, tr: Throwable?): Int =
            println(WARN, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun w(tag: String?, tr: Throwable?): Int =
            println(WARN, tag, getStackTraceString(tr))

        @JvmStatic
        fun e(tag: String?, msg: String): Int = println(ERROR, tag, msg)

        @JvmStatic
        fun e(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ERROR, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun wtf(tag: String?, msg: String?): Int = wtf(tag, msg, null)

        @JvmStatic
        fun wtf(tag: String?, tr: Throwable): Int = wtf(tag, tr.message, tr)

        @JvmStatic
        fun wtf(tag: String?, msg: String?, tr: Throwable?): Int =
            println(ASSERT, tag, "${msg ?: ""}\n${getStackTraceString(tr)}")

        @JvmStatic
        fun getStackTraceString(tr: Throwable?): String {
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

        @JvmStatic
        fun println(priority: Int, tag: String?, msg: String): Int {
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
            return formatted.toByteArray(Charsets.UTF_8).size
        }

        @JvmStatic
        fun isLoggable(tag: String?, level: Int): Boolean {
            return level >= JakoApplication.config.logMinPriority
        }
    }
}
