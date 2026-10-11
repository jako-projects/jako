package java.util.logging

actual abstract class Handler protected actual constructor() {
    private var handlerLevel: Level = Level.ALL

    actual abstract fun publish(record: LogRecord)
    actual abstract fun flush()
    actual abstract fun close()

    actual open fun getLevel(): Level = handlerLevel
    actual open fun setLevel(newLevel: Level) { this.handlerLevel = newLevel }
    actual open var level: Level
        get() = getLevel()
        set(value) = setLevel(value)
}
