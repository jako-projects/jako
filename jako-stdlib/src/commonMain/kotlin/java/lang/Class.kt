@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.Serializable

@Suppress("UPPER_BOUND_CANNOT_BE_ARRAY")
expect class Class<T> : Serializable {
    fun getName(): String
    fun getSimpleName(): String
    fun getCanonicalName(): String?
    fun isPrimitive(): Boolean
    fun isArray(): Boolean
    fun isInstance(obj: Any?): Boolean
    fun getComponentType(): Class<*>?
    fun cast(obj: Any?): T?

    val name: String
    val simpleName: String
    val canonicalName: String?
    val isPrimitive: Boolean
    val isArray: Boolean
    val componentType: Class<*>?

    companion object {
        fun forName(className: String): Class<*>
    }
}
