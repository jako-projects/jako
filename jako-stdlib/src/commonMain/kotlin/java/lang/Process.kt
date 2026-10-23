@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

expect abstract class Process() {
    abstract fun getOutputStream(): OutputStream
    abstract fun getInputStream(): InputStream
    abstract fun getErrorStream(): InputStream
    abstract fun waitFor(): Int
    open fun waitFor(timeout: Long, unit: TimeUnit): Boolean
    abstract fun exitValue(): Int
    abstract fun destroy()
    open fun destroyForcibly(): Process
    open fun isAlive(): Boolean

    open val outputStream: OutputStream
    open val inputStream: InputStream
    open val errorStream: InputStream
    open val isAlive: Boolean
}
