@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.io.Serializable
import java.lang.Cloneable

expect abstract class Format : Serializable, Cloneable {
    protected constructor()
    open fun format(obj: Any?): String
    open fun parseObject(source: String, pos: ParsePosition): Any?
    open fun parseObject(source: String): Any?
}
