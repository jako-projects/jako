@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class FormattableFlags {
    companion object {
        val LEFT_JUSTIFY: Int
        val UPPERCASE: Int
        val ALTERNATE: Int
    }
}
