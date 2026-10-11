@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

expect abstract class Handler {
    protected constructor()
    abstract fun publish(record: LogRecord)
    abstract fun flush()
    abstract fun close()
    open fun getLevel(): Level
    open fun setLevel(newLevel: Level)
    open var level: Level
}
