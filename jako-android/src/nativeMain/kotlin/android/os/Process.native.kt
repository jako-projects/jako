package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual open class Process actual constructor() {
    actual companion object {
        actual val THREAD_PRIORITY_DEFAULT: Int = 0
        actual val THREAD_PRIORITY_LOWEST: Int = 19
        actual val THREAD_PRIORITY_BACKGROUND: Int = 10
        actual val THREAD_PRIORITY_FOREGROUND: Int = -2
        actual val THREAD_PRIORITY_DISPLAY: Int = -4
        actual val THREAD_PRIORITY_URGENT_DISPLAY: Int = -8
        actual val THREAD_PRIORITY_VIDEO: Int = -10
        actual val THREAD_PRIORITY_AUDIO: Int = -16
        actual val THREAD_PRIORITY_URGENT_AUDIO: Int = -19
        actual val THREAD_PRIORITY_MORE_FAVORABLE: Int = -1
        actual val THREAD_PRIORITY_LESS_FAVORABLE: Int = 1

        actual fun myPid(): Int = getpid()
        actual fun myTid(): Int = Thread.currentThread().id.toInt()
        actual fun myUid(): Int = 0
        actual fun is64Bit(): Boolean = sizeOf<COpaquePointerVar>() == 8L
    }
}
