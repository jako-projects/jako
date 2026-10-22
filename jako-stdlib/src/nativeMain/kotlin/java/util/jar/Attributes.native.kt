package java.util.jar

import java.lang.Cloneable

actual open class Attributes : MutableMap<Any, Any>, Cloneable {
    protected val map: MutableMap<Any, Any>

    actual constructor() : this(11)

    actual constructor(size: Int) {
        map = LinkedHashMap(size)
    }

    actual constructor(attr: Attributes) {
        map = LinkedHashMap(attr.map)
    }

    actual override val size: Int get() = map.size
    actual override fun isEmpty(): Boolean = map.isEmpty()
    actual override fun containsKey(key: Any): Boolean = map.containsKey(key)
    actual override fun containsValue(value: Any): Boolean = map.containsValue(value)
    actual override fun get(key: Any): Any? = map[key]

    actual open fun getValue(name: String): String? = map[Name(name)] as? String
    actual open fun getValue(name: Name): String? = map[name] as? String

    actual override fun put(key: Any, value: Any): Any? {
        if (key !is Name) throw ClassCastException("Attributes key must be Attributes.Name")
        if (value !is String) throw ClassCastException("Attributes value must be String")
        return map.put(key, value)
    }

    actual open fun putValue(name: String, value: String): String? {
        return put(Name(name), value) as? String
    }

    actual override fun remove(key: Any): Any? = map.remove(key)

    actual override fun putAll(from: Map<out Any, Any>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }

    actual override fun clear() = map.clear()

    actual override val keys: MutableSet<Any> get() = map.keys
    actual override val values: MutableCollection<Any> get() = map.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>> get() = map.entries

    actual open fun clone(): Any = Attributes(this)

    override fun equals(other: Any?): Boolean = map == other
    override fun hashCode(): Int = map.hashCode()

    actual open class Name actual constructor(private val name: String) {
        private val hashValue: Int

        init {
            if (name.isEmpty() || name.length > 70) {
                throw IllegalArgumentException("Invalid attribute name: $name")
            }
            var h = 0
            for (i in name.indices) {
                val c = name[i]
                val valid = (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9') || c == '_' || c == '-'
                if (!valid) {
                    throw IllegalArgumentException("Invalid character in attribute name: $name")
                }
                val lower = if (c in 'A'..'Z') (c.code + 32) else c.code
                h = 31 * h + lower
            }
            hashValue = h
        }

        actual override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Name) return false
            return name.equals(other.name, ignoreCase = true)
        }

        actual override fun hashCode(): Int = hashValue

        actual override fun toString(): String = name

        actual companion object {
            actual val MANIFEST_VERSION: Name = Name("Manifest-Version")
            actual val SIGNATURE_VERSION: Name = Name("Signature-Version")
            actual val CONTENT_TYPE: Name = Name("Content-Type")
            actual val CLASS_PATH: Name = Name("Class-Path")
            actual val MAIN_CLASS: Name = Name("Main-Class")
            actual val SEALED: Name = Name("Sealed")
            actual val EXTENSION_LIST: Name = Name("Extension-List")
            actual val EXTENSION_NAME: Name = Name("Extension-Name")
            actual val IMPLEMENTATION_TITLE: Name = Name("Implementation-Title")
            actual val IMPLEMENTATION_VERSION: Name = Name("Implementation-Version")
            actual val IMPLEMENTATION_VENDOR: Name = Name("Implementation-Vendor")
            actual val SPECIFICATION_TITLE: Name = Name("Specification-Title")
            actual val SPECIFICATION_VERSION: Name = Name("Specification-Version")
            actual val SPECIFICATION_VENDOR: Name = Name("Specification-Vendor")
        }
    }
}
