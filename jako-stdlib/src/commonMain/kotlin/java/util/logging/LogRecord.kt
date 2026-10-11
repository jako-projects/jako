@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

import java.io.Serializable

expect open class LogRecord(level: Level, msg: String) : Serializable {
    open fun getLevel(): Level
    open fun setLevel(level: Level)
    open fun getLoggerName(): String?
    open fun setLoggerName(name: String?)
    open fun getMessage(): String
    open fun setMessage(message: String)
    open fun getMillis(): Long
    open fun setMillis(millis: Long)
    open fun getThrown(): Throwable?
    open fun setThrown(thrown: Throwable?)

    open var level: Level
    open var loggerName: String?
    open var message: String
    open var millis: Long
    open var thrown: Throwable?
}
