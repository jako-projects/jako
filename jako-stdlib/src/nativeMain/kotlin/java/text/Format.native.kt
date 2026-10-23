package java.text

import java.io.Serializable
import java.lang.Cloneable

actual abstract class Format protected actual constructor() : Serializable, Cloneable {
    actual open fun format(obj: Any?): String = obj.toString()
    actual open fun parseObject(source: String, pos: ParsePosition): Any? = null
    actual open fun parseObject(source: String): Any? = parseObject(source, ParsePosition(0))
}
