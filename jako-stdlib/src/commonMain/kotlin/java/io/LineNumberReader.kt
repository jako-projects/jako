@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class LineNumberReader : BufferedReader {
    constructor(inReader: Reader)
    constructor(inReader: Reader, sz: Int)
    open fun setLineNumber(lineNumber: Int)
    open fun getLineNumber(): Int
    open val lineNumber: Int
}
