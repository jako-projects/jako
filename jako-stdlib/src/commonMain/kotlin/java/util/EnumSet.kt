@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Class
import java.lang.Cloneable

/**
 * [Nullability Decision] EnumSet does not permit null elements (E : Enum<E>).
 */
expect abstract class EnumSet<E : Enum<E>> : AbstractMutableSet<E>, Cloneable, Serializable {
    open fun clone(): EnumSet<E>

    companion object {
        fun <E : Enum<E>> noneOf(elementType: Class<E>): EnumSet<E>
        fun <E : Enum<E>> copyOf(s: EnumSet<E>): EnumSet<E>
        fun <E : Enum<E>> copyOf(c: Collection<E>): EnumSet<E>
        fun <E : Enum<E>> of(e: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E): EnumSet<E>
        fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet<E>
        fun <E : Enum<E>> of(first: E, vararg rest: E): EnumSet<E>
    }
}
