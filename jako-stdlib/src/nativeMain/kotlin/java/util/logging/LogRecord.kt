package java.util.logging

import java.io.Serializable

actual open class LogRecord actual constructor(
    level: Level,
    msg: String
) : Serializable {
    private var lvl: Level = level
    private var messageStr: String = msg
    private var logName: String? = null
    private var timeMillis: Long = 0L
    private var thr: Throwable? = null

    actual open fun getLevel(): Level = lvl
    actual open fun setLevel(level: Level) { this.lvl = level }
    actual open fun getLoggerName(): String? = logName
    actual open fun setLoggerName(name: String?) { this.logName = name }
    actual open fun getMessage(): String = messageStr
    actual open fun setMessage(message: String) { this.messageStr = message }
    actual open fun getMillis(): Long = timeMillis
    actual open fun setMillis(millis: Long) { this.timeMillis = millis }
    actual open fun getThrown(): Throwable? = thr
    actual open fun setThrown(thrown: Throwable?) { this.thr = thrown }

    actual open var level: Level
        get() = getLevel()
        set(value) = setLevel(value)
    actual open var loggerName: String?
        get() = getLoggerName()
        set(value) = setLoggerName(value)
    actual open var message: String
        get() = getMessage()
        set(value) = setMessage(value)
    actual open var millis: Long
        get() = getMillis()
        set(value) = setMillis(value)
    actual open var thrown: Throwable?
        get() = getThrown()
        set(value) = setThrown(value)
}
