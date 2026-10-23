@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect fun interface Formattable {
    fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int)
}
