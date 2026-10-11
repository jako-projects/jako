@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

expect open class SimpleFormatter() : Formatter {
    override fun format(record: LogRecord): String
}
