package java.lang

import com.bennyhuo.jako.NodeBridge
import java.io.File
import java.util.StringTokenizer

actual class Runtime private constructor() {
    actual fun exec(command: String): Process = exec(command, null, null)

    actual fun exec(command: String, envp: Array<String>?): Process = exec(command, envp, null)

    actual fun exec(command: String, envp: Array<String>?, dir: File?): Process {
        if (command.isEmpty()) {
            throw IllegalArgumentException("Empty command")
        }
        val st = StringTokenizer(command)
        val cmdarray = Array(st.countTokens()) { st.nextToken() }
        return exec(cmdarray, envp, dir)
    }

    actual fun exec(cmdarray: Array<String>): Process = exec(cmdarray, null, null)

    actual fun exec(cmdarray: Array<String>, envp: Array<String>?): Process = exec(cmdarray, envp, null)

    actual fun exec(cmdarray: Array<String>, envp: Array<String>?, dir: File?): Process {
        val pb = ProcessBuilder(*cmdarray).directory(dir)
        if (envp != null) {
            val env = pb.environment()
            env.clear()
            for (entry in envp) {
                val idx = entry.indexOf('=')
                if (idx != -1) {
                    env[entry.substring(0, idx)] = entry.substring(idx + 1)
                }
            }
        }
        return pb.start()
    }

    actual fun availableProcessors(): Int = NodeBridge.availableProcessors()

    actual fun freeMemory(): Long = 64L * 1024L * 1024L

    actual fun totalMemory(): Long = 256L * 1024L * 1024L

    actual fun maxMemory(): Long = 1024L * 1024L * 1024L

    actual fun gc() {}

    actual fun exit(status: Int) {
        NodeBridge.exit(status)
    }

    actual fun halt(status: Int) {
        NodeBridge.exit(status)
    }

    actual companion object {
        private val instance = Runtime()
        actual fun getRuntime(): Runtime = instance
    }
}
