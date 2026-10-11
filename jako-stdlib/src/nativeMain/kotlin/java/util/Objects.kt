package java.util

actual class Objects private constructor() {
    actual companion object {
        actual fun equals(a: Any?, b: Any?): Boolean = a == b || (a != null && a == b)

        actual fun hashCode(o: Any?): Int = o?.hashCode() ?: 0

        actual fun hash(vararg values: Any?): Int = Arrays.hashCode(values)

        actual fun toString(o: Any?): String = o?.toString() ?: "null"

        actual fun toString(o: Any?, nullDefault: String): String = o?.toString() ?: nullDefault

        actual fun <T> compare(a: T?, b: T?, c: Comparator<in T>): Int {
            return if (a === b) 0 else c.compare(a as T, b as T)
        }

        actual fun <T> requireNonNull(obj: T?): T {
            if (obj == null) throw NullPointerException()
            return obj
        }

        actual fun <T> requireNonNull(obj: T?, message: String): T {
            if (obj == null) throw NullPointerException(message)
            return obj
        }

        actual fun isNull(obj: Any?): Boolean = obj == null

        actual fun nonNull(obj: Any?): Boolean = obj != null

        actual fun <T> requireNonNullElse(obj: T?, defaultObj: T): T {
            return obj ?: requireNonNull(defaultObj, "defaultObj")
        }
    }
}
