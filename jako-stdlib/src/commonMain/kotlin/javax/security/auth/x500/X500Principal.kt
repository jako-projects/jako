@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.security.auth.x500

import java.io.Serializable
import java.security.Principal

expect class X500Principal(name: String) : Principal, Serializable {
    override fun getName(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}
