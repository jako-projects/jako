@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * Android `android.util.Log` API declaration for Kotlin Multiplatform.
 *
 * [Nullability Decision]:
 * - `tag`: nullable `String?` (Android Log allows null tag).
 * - `msg`: non-null `String` for standard `v/d/i/w/e(tag, msg)`, nullable `String?` for overloads with `Throwable?` and `wtf`.
 * - `tr`: nullable `Throwable?` (except `wtf(tag, tr)` where `tr` is non-null).
 * - `getStackTraceString(tr: Throwable?)`: returns non-null `String` (`""` when `tr == null`).
 */
expect class Log {
    companion object {
        val VERBOSE: Int
        val DEBUG: Int
        val INFO: Int
        val WARN: Int
        val ERROR: Int
        val ASSERT: Int

        fun v(tag: String?, msg: String): Int
        fun v(tag: String?, msg: String?, tr: Throwable?): Int
        fun d(tag: String?, msg: String): Int
        fun d(tag: String?, msg: String?, tr: Throwable?): Int
        fun i(tag: String?, msg: String): Int
        fun i(tag: String?, msg: String?, tr: Throwable?): Int
        fun w(tag: String?, msg: String): Int
        fun w(tag: String?, msg: String?, tr: Throwable?): Int
        fun w(tag: String?, tr: Throwable?): Int
        fun e(tag: String?, msg: String): Int
        fun e(tag: String?, msg: String?, tr: Throwable?): Int
        fun wtf(tag: String?, msg: String?): Int
        fun wtf(tag: String?, tr: Throwable): Int
        fun wtf(tag: String?, msg: String?, tr: Throwable?): Int
        fun getStackTraceString(tr: Throwable?): String
        fun println(priority: Int, tag: String?, msg: String): Int
        fun isLoggable(tag: String?, level: Int): Boolean
    }
}
