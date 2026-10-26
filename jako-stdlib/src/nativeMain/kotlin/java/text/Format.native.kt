package java.text

import java.io.Serializable
import java.lang.Cloneable

actual abstract class Format protected actual constructor() : Serializable, Cloneable {
    actual open fun format(obj: Any?): String = obj.toString()
    actual open fun parseObject(source: String, pos: ParsePosition): Any? = null

    @Throws(ParseException::class)
    actual open fun parseObject(source: String): Any? {
        val pos = ParsePosition(0)
        val result = parseObject(source, pos)
        if (pos.getIndex() == 0) {
            throw ParseException("Format.parseObject(String) failed", pos.getErrorIndex())
        }
        return result
    }
}
