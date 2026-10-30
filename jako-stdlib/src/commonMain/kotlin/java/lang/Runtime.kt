@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.File

expect class Runtime {
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(command: String): Process
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(command: String, envp: Array<String>?): Process
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(command: String, envp: Array<String>?, dir: File?): Process
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(cmdarray: Array<String>): Process
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(cmdarray: Array<String>, envp: Array<String>?): Process
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun exec(cmdarray: Array<String>, envp: Array<String>?, dir: File?): Process
    fun availableProcessors(): Int
    fun freeMemory(): Long
    fun totalMemory(): Long
    fun maxMemory(): Long
    fun gc()
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not support terminating the host process")
    fun exit(status: Int)
    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not support terminating the host process")
    fun halt(status: Int)

    companion object {
        fun getRuntime(): Runtime
    }
}
