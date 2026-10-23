@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.File

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
