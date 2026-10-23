package android.os

open class Process {
    companion object {
        const val THREAD_PRIORITY_DEFAULT: Int = 0
        const val THREAD_PRIORITY_LOWEST: Int = 19
        const val THREAD_PRIORITY_BACKGROUND: Int = 10
        const val THREAD_PRIORITY_FOREGROUND: Int = -2
        const val THREAD_PRIORITY_DISPLAY: Int = -4
        const val THREAD_PRIORITY_URGENT_DISPLAY: Int = -8
        const val THREAD_PRIORITY_VIDEO: Int = -10
        const val THREAD_PRIORITY_AUDIO: Int = -16
        const val THREAD_PRIORITY_URGENT_AUDIO: Int = -19
        const val THREAD_PRIORITY_MORE_FAVORABLE: Int = -1
        const val THREAD_PRIORITY_LESS_FAVORABLE: Int = 1

        @JvmStatic
        fun myPid(): Int = ProcessHandle.current().pid().toInt()

        @JvmStatic
        fun myTid(): Int = Thread.currentThread().id.toInt()

        @JvmStatic
        fun myUid(): Int = 0

        @JvmStatic
        fun is64Bit(): Boolean = System.getProperty("sun.arch.data.model") == "64" ||
            System.getProperty("os.arch")?.contains("64") == true
    }
}
