@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

expect abstract class Formatter {
    protected constructor()
    abstract fun format(record: LogRecord): String
    open fun formatMessage(record: LogRecord): String
}
