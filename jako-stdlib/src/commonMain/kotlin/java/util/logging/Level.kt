@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

import java.io.Serializable

expect open class Level : Serializable {
    open fun getName(): String
    open fun intValue(): Int
    open val name: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        val OFF: Level
        val SEVERE: Level
        val WARNING: Level
        val INFO: Level
        val CONFIG: Level
        val FINE: Level
        val FINER: Level
        val FINEST: Level
        val ALL: Level

        fun parse(name: String): Level
    }
}
