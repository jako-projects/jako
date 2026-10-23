package kotlin.io

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

public fun Reader.readText(): String {
    val buffer = StringWriter()
    copyTo(buffer)
    return buffer.toString()
}

public fun Reader.readLines(): List<String> {
    val result = ArrayList<String>()
    forEachLine { result.add(it) }
    return result
}

public fun Reader.copyTo(out: Writer, bufferSize: Int = 8192): Long {
    var charsCopied: Long = 0
    val buffer = CharArray(bufferSize)
    var chars = read(buffer)
    while (chars >= 0) {
        out.write(buffer, 0, chars)
        charsCopied += chars
        chars = read(buffer)
    }
    return charsCopied
}

public fun Reader.forEachLine(action: (String) -> Unit) {
    buffered().lineSequence().forEach(action)
}

public fun <T> Reader.useLines(block: (Sequence<String>) -> T): T {
    val r = buffered()
    try {
        return block(r.lineSequence())
    } finally {
        r.close()
    }
}

public fun Reader.buffered(bufferSize: Int = 8192): BufferedReader =
    if (this is BufferedReader) this else BufferedReader(this, bufferSize)

public fun BufferedReader.lineSequence(): Sequence<String> = Sequence {
    object : Iterator<String> {
        private var nextValue: String? = null
        private var done = false

        override fun hasNext(): Boolean {
            if (nextValue == null && !done) {
                nextValue = readLine()
                if (nextValue == null) done = true
            }
            return nextValue != null
        }

        override fun next(): String {
            if (!hasNext()) throw NoSuchElementException()
            val answer = nextValue
            nextValue = null
            return answer!!
        }
    }
}

public fun BufferedReader.forEachLine(action: (String) -> Unit) {
    lineSequence().forEach(action)
}

public fun Writer.buffered(bufferSize: Int = 8192): BufferedWriter =
    if (this is BufferedWriter) this else BufferedWriter(this, bufferSize)
