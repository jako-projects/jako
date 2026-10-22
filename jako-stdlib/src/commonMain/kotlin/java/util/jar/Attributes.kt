@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.jar

import java.lang.Cloneable

/**
 * [Nullability Decision] Attributes maps Name keys to String values; getValue and putValue return String? for absent/previous values.
 */
expect open class Attributes : MutableMap<Any, Any>, Cloneable {
    constructor()
    constructor(size: Int)
    constructor(attr: Attributes)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: Any): Boolean
    override fun containsValue(value: Any): Boolean
    override fun get(key: Any): Any?
    open fun getValue(name: String): String?
    open fun getValue(name: Name): String?
    override fun put(key: Any, value: Any): Any?
    open fun putValue(name: String, value: String): String?
    override fun remove(key: Any): Any?
    override fun putAll(from: Map<out Any, Any>)
    override fun clear()
    override val keys: MutableSet<Any>
    override val values: MutableCollection<Any>
    override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
    open fun clone(): Any

    open class Name(name: String) {
        override fun equals(other: Any?): Boolean
        override fun hashCode(): Int
        override fun toString(): String

        companion object {
            val MANIFEST_VERSION: Name
            val SIGNATURE_VERSION: Name
            val CONTENT_TYPE: Name
            val CLASS_PATH: Name
            val MAIN_CLASS: Name
            val SEALED: Name
            val EXTENSION_LIST: Name
            val EXTENSION_NAME: Name
            val IMPLEMENTATION_TITLE: Name
            val IMPLEMENTATION_VERSION: Name
            val IMPLEMENTATION_VENDOR: Name
            val SPECIFICATION_TITLE: Name
            val SPECIFICATION_VERSION: Name
            val SPECIFICATION_VENDOR: Name
        }
    }
}
