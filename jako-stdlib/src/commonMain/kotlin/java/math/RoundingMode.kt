@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

expect enum class RoundingMode {
    UP,
    DOWN,
    CEILING,
    FLOOR,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN,
    UNNECESSARY;

    companion object {
        fun valueOf(rm: Int): RoundingMode
    }
}
