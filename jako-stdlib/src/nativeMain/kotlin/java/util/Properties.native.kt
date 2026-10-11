package java.util

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Reader
import java.io.Serializable
import java.io.Writer

actual open class Properties actual constructor(
    private val defaults: Properties?
) : Serializable {

    actual constructor() : this(null)

    private val map = mutableMapOf<Any, Any>()

    actual open fun setProperty(key: String, value: String): Any? {
        return map.put(key, value)
    }

    actual open fun getProperty(key: String): String? {
        val oval = map[key]
        val sval = if (oval is String) oval else null
        return sval ?: defaults?.getProperty(key)
    }

    actual open fun getProperty(key: String, defaultValue: String): String {
        val v = getProperty(key)
        return v ?: defaultValue
    }

    actual open fun propertyNames(): Enumeration<*> {
        val allKeys = mutableSetOf<Any>()
        allKeys.addAll(map.keys)
        if (defaults != null) {
            val enum = defaults.propertyNames()
            while (enum.hasMoreElements()) {
                allKeys.add(enum.nextElement() as Any)
            }
        }
        val iterator = allKeys.iterator()
        return object : Enumeration<Any> {
            override fun hasMoreElements(): Boolean = iterator.hasNext()
            override fun nextElement(): Any = iterator.next()
        }
    }

    actual open fun stringPropertyNames(): Set<String> {
        val result = mutableSetOf<String>()
        for (k in map.keys) {
            if (k is String) {
                result.add(k)
            }
        }
        if (defaults != null) {
            result.addAll(defaults.stringPropertyNames())
        }
        return result
    }

    actual open fun load(inStream: InputStream) {
        load(InputStreamReader(inStream, "ISO-8859-1"))
    }

    actual open fun load(reader: Reader) {
        val bufferedReader = if (reader is BufferedReader) reader else BufferedReader(reader)
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            val trimmed = line.trimStart()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!")) {
                var sepIdx = -1
                for (i in trimmed.indices) {
                    val c = trimmed[i]
                    if (c == '=' || c == ':') {
                        sepIdx = i
                        break
                    }
                }
                if (sepIdx != -1) {
                    val key = unescape(trimmed.substring(0, sepIdx).trim())
                    val value = unescape(trimmed.substring(sepIdx + 1).trim())
                    map[key] = value
                } else {
                    map[unescape(trimmed.trim())] = ""
                }
            }
            line = bufferedReader.readLine()
        }
    }

    actual open fun store(out: OutputStream, comments: String?) {
        store(OutputStreamWriter(out, "ISO-8859-1"), comments)
    }

    actual open fun store(writer: Writer, comments: String?) {
        val bw = if (writer is BufferedWriter) writer else BufferedWriter(writer)
        if (comments != null) {
            bw.write("#$comments")
            bw.newLine()
        }
        bw.write("#${Date()}")
        bw.newLine()
        for ((k, v) in map) {
            val keyStr = escape(k.toString(), isKey = true)
            val valStr = escape(v.toString(), isKey = false)
            bw.write("$keyStr=$valStr")
            bw.newLine()
        }
        bw.flush()
    }

    actual open fun list(out: PrintStream) {
        out.println("-- listing properties --")
        for ((k, v) in map) {
            out.println("$k=$v")
        }
    }

    actual open fun list(out: PrintWriter) {
        out.println("-- listing properties --")
        for ((k, v) in map) {
            out.println("$k=$v")
        }
    }

    actual open fun clear() {
        map.clear()
    }

    actual open fun size(): Int = map.size

    actual open fun isEmpty(): Boolean = map.isEmpty()

    actual open fun containsKey(key: Any): Boolean = map.containsKey(key)

    actual open fun get(key: Any): Any? = map[key]

    actual open fun put(key: Any, value: Any): Any? = map.put(key, value)

    actual open fun remove(key: Any): Any? = map.remove(key)

    private fun escape(s: String, isKey: Boolean): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '\t' -> sb.append("\\t")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '=', ':' -> if (isKey) sb.append('\\').append(c) else sb.append(c)
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                val next = s[i + 1]
                when (next) {
                    't' -> { sb.append('\t'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    '\\', '=', ':' -> { sb.append(next); i += 2 }
                    'u' -> {
                        if (i + 5 < s.length) {
                            val code = s.substring(i + 2, i + 6).toIntOrNull(16)
                            if (code != null) {
                                sb.append(code.toChar())
                                i += 6
                                continue
                            }
                        }
                        sb.append(c)
                        i++
                    }
                    else -> { sb.append(next); i += 2 }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}
