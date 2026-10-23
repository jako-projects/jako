@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.File

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
