@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.regex.Pattern

expect open class Scanner : Iterator<String>, Closeable {
    constructor(source: java.lang.Readable)
    constructor(source: String)
    constructor(source: InputStream)
    constructor(source: InputStream, charsetName: String)
    constructor(source: File)
    constructor(source: File, charsetName: String)
    constructor(source: Path)
    constructor(source: Path, charsetName: String)

    override fun hasNext(): Boolean
    override fun next(): String
    open fun hasNextLine(): Boolean
    open fun nextLine(): String
    open fun hasNextInt(): Boolean
    open fun nextInt(): Int
    open fun hasNextLong(): Boolean
    open fun nextLong(): Long
    open fun hasNextDouble(): Boolean
    open fun nextDouble(): Double
    open fun hasNextBoolean(): Boolean
    open fun nextBoolean(): Boolean
    open fun useDelimiter(pattern: String): Scanner
    open fun useDelimiter(pattern: Pattern): Scanner
    open fun delimiter(): Pattern
    override fun close()
}

expect open class InputMismatchException : NoSuchElementException {
    constructor()
    constructor(s: String?)
}
