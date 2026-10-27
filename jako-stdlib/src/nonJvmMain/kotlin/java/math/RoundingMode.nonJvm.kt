package java.math

actual enum class RoundingMode(val oldMode: Int) {
    UP(0),
    DOWN(1),
    CEILING(2),
    FLOOR(3),
    HALF_UP(4),
    HALF_DOWN(5),
    HALF_EVEN(6),
    UNNECESSARY(7);

    actual companion object {
        actual fun valueOf(rm: Int): RoundingMode {
            return when (rm) {
                0 -> UP
                1 -> DOWN
                2 -> CEILING
                3 -> FLOOR
                4 -> HALF_UP
                5 -> HALF_DOWN
                6 -> HALF_EVEN
                7 -> UNNECESSARY
                else -> throw IllegalArgumentException("argument out of range")
            }
        }
    }
}
