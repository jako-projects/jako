package java.util

import java.io.Serializable

actual open class EventObject actual constructor(source: Any?) : Serializable {
    private val src: Any

    init {
        if (source == null) throw IllegalArgumentException("null source")
        src = source
    }

    actual open fun getSource(): Any = src
    actual open val source: Any get() = src

    actual override fun toString(): String {
        val className = this::class.qualifiedName ?: "java.util.EventObject"
        return "$className[source=$src]"
    }
}
