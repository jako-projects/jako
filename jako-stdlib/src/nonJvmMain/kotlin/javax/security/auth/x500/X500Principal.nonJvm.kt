package javax.security.auth.x500

import java.security.Principal
import java.io.Serializable

actual class X500Principal actual constructor(name: String) : Principal, Serializable {
    private val nameStr: String = name

    actual override fun getName(): String = nameStr
    actual override fun equals(other: Any?): Boolean = other is X500Principal && other.nameStr == nameStr
    actual override fun hashCode(): Int = nameStr.hashCode()
    actual override fun toString(): String = nameStr
}
