@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class Objects {
    companion object {
        fun equals(a: Any?, b: Any?): Boolean
        fun deepEquals(a: Any?, b: Any?): Boolean
        fun hashCode(o: Any?): Int
        fun hash(vararg values: Any?): Int
        fun toString(o: Any?): String
        fun toString(o: Any?, nullDefault: String): String
        fun <T> compare(a: T?, b: T?, c: Comparator<in T>): Int
        fun <T> requireNonNull(obj: T?): T
        fun <T> requireNonNull(obj: T?, message: String): T
        fun <T> requireNonNull(obj: T?, messageSupplier: java.util.function.Supplier<String>): T
        fun isNull(obj: Any?): Boolean
        fun nonNull(obj: Any?): Boolean
    }
}
