package java.lang

import java.io.Serializable
import kotlin.reflect.KClass

/**
 * [Nullability Decision] Relaxed from `T : Any` to `T` to match JDK 8 Class<T>
 * where T can be any type including nullable projections.
 */
@Suppress("UPPER_BOUND_CANNOT_BE_ARRAY")
actual class Class<T>(val kClass: KClass<out Any>) : Serializable {
    actual fun getName(): String = kClass.simpleName ?: "Unknown"
    actual fun getSimpleName(): String = kClass.simpleName ?: "Unknown"
    actual val name: String get() = getName()
    actual val simpleName: String get() = getSimpleName()

    @Suppress("UNCHECKED_CAST")
    actual fun <U> cast(obj: Any?): U? = obj as? U

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Class<*>) return false
        return kClass == other.kClass
    }

    override fun hashCode(): Int = kClass.hashCode()
    override fun toString(): String = "class $name"

    actual companion object {
        actual fun forName(className: String): Class<*> {
            throw ClassNotFoundException(className)
        }
    }
}

actual open class ClassNotFoundException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
    actual constructor(s: String?, ex: Throwable?) : super(s, ex)
}

