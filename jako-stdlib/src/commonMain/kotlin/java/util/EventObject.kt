@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable

/**
 * [Nullability Decision] EventObject constructor throws IllegalArgumentException if source is null; getSource() returns non-null Any.
 */
expect open class EventObject(source: Any?) : Serializable {
    open fun getSource(): Any
    open val source: Any
    override fun toString(): String
}
