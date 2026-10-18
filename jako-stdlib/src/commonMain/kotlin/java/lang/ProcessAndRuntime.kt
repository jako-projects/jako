@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.File
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

expect class ProcessBuilder {
    constructor(vararg command: String)
    constructor(command: MutableList<String>)

    fun command(command: MutableList<String>): ProcessBuilder
    fun command(vararg command: String): ProcessBuilder
    fun command(): MutableList<String>
    fun environment(): MutableMap<String, String>
    fun directory(): File?
    fun directory(directory: File?): ProcessBuilder
    fun redirectErrorStream(): Boolean
    fun redirectErrorStream(redirectErrorStream: Boolean): ProcessBuilder
    fun inheritIO(): ProcessBuilder
    fun start(): Process
}

expect class Runtime {
    fun exec(command: String): Process
    fun exec(command: String, envp: Array<String>?): Process
    fun exec(command: String, envp: Array<String>?, dir: File?): Process
    fun exec(cmdarray: Array<String>): Process
    fun exec(cmdarray: Array<String>, envp: Array<String>?): Process
    fun exec(cmdarray: Array<String>, envp: Array<String>?, dir: File?): Process
    fun availableProcessors(): Int
    fun freeMemory(): Long
    fun totalMemory(): Long
    fun maxMemory(): Long
    fun gc()
    fun exit(status: Int)
    fun halt(status: Int)

    companion object {
        fun getRuntime(): Runtime
    }
}
