package java.util

import java.io.Serializable
import java.lang.Class
import java.lang.Cloneable

actual abstract class EnumSet<E : Enum<E>> protected constructor() : AbstractMutableSet<E>(), Cloneable, Serializable {
    actual open fun clone(): EnumSet<E> {
        return copyOf(this)
    }

    private class RegularEnumSet<E : Enum<E>> : EnumSet<E>() {
        private val backing = TreeSet<E>(Comparator { a, b -> a.ordinal.compareTo(b.ordinal) })
        override val size: Int get() = backing.size
        override fun iterator(): MutableIterator<E> = backing.iterator()
        override fun add(element: E): Boolean = backing.add(element)
        override fun remove(element: E): Boolean = backing.remove(element)
        override fun contains(element: E): Boolean = backing.contains(element)
        override fun clear() = backing.clear()
    }

    actual companion object {
        actual fun <E : Enum<E>> noneOf(elementType: Class<E>): EnumSet<E> {
            return RegularEnumSet()
        }

        actual fun <E : Enum<E>> copyOf(s: EnumSet<E>): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.addAll(s)
            return res
        }

        actual fun <E : Enum<E>> copyOf(c: Collection<E>): EnumSet<E> {
            if (c is EnumSet<E>) return copyOf(c)
            if (c.isEmpty()) throw IllegalArgumentException("Collection is empty")
            val res = RegularEnumSet<E>()
            res.addAll(c)
            return res
        }

        actual fun <E : Enum<E>> of(e: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            res.add(e4)
            return res
        }

        actual fun <E : Enum<E>> of(e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(e1)
            res.add(e2)
            res.add(e3)
            res.add(e4)
            res.add(e5)
            return res
        }

        actual fun <E : Enum<E>> of(first: E, vararg rest: E): EnumSet<E> {
            val res = RegularEnumSet<E>()
            res.add(first)
            for (e in rest) {
                res.add(e)
            }
            return res
        }
    }
}
