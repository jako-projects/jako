package java.util.logging

import java.io.Serializable

actual open class Level protected constructor(
    private val nameStr: String,
    private val value: Int
) : Serializable {

    actual open fun getName(): String = nameStr
    actual open fun intValue(): Int = value
    actual open val name: String get() = nameStr

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Level) return false
        return value == other.value
    }

    actual override fun hashCode(): Int = value

    actual override fun toString(): String = nameStr

    actual companion object {
        actual val OFF: Level = Level("OFF", Int.MAX_VALUE)
        actual val SEVERE: Level = Level("SEVERE", 1000)
        actual val WARNING: Level = Level("WARNING", 900)
        actual val INFO: Level = Level("INFO", 800)
        actual val CONFIG: Level = Level("CONFIG", 700)
        actual val FINE: Level = Level("FINE", 500)
        actual val FINER: Level = Level("FINER", 400)
        actual val FINEST: Level = Level("FINEST", 300)
        actual val ALL: Level = Level("ALL", Int.MIN_VALUE)

        actual fun parse(name: String): Level {
            return when (name.uppercase()) {
                "OFF" -> OFF
                "SEVERE" -> SEVERE
                "WARNING" -> WARNING
                "INFO" -> INFO
                "CONFIG" -> CONFIG
                "FINE" -> FINE
                "FINER" -> FINER
                "FINEST" -> FINEST
                "ALL" -> ALL
                else -> {
                    val intVal = name.toIntOrNull() ?: throw IllegalArgumentException("Bad level: $name")
                    Level(name, intVal)
                }
            }
        }
    }
}
