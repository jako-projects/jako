@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Tools for managing OS processes.
 */
expect open class Process() {
    companion object {
        val THREAD_PRIORITY_DEFAULT: Int
        val THREAD_PRIORITY_LOWEST: Int
        val THREAD_PRIORITY_BACKGROUND: Int
        val THREAD_PRIORITY_FOREGROUND: Int
        val THREAD_PRIORITY_DISPLAY: Int
        val THREAD_PRIORITY_URGENT_DISPLAY: Int
        val THREAD_PRIORITY_VIDEO: Int
        val THREAD_PRIORITY_AUDIO: Int
        val THREAD_PRIORITY_URGENT_AUDIO: Int
        val THREAD_PRIORITY_MORE_FAVORABLE: Int
        val THREAD_PRIORITY_LESS_FAVORABLE: Int

        fun myPid(): Int
        fun myTid(): Int
        fun myUid(): Int
        fun is64Bit(): Boolean
    }
}
