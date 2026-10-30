@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.File

@WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
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
    @WebSupport(WebRuntime.NODE_JS, reason = "Spawning OS child processes requires Node.js child_process.spawnSync")
    fun start(): Process
}
