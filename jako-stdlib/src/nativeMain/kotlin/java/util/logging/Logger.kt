package java.util.logging

actual open class Logger protected constructor(
    private val nameStr: String
) {
    private var levelObj: Level? = null

    actual open fun getName(): String = nameStr
    actual open val name: String get() = nameStr
    actual open fun getLevel(): Level? = levelObj
    actual open fun setLevel(newLevel: Level?) { this.levelObj = newLevel }

    actual open fun isLoggable(level: Level): Boolean {
        val effectiveLevel = levelObj ?: Level.INFO
        return level.intValue() >= effectiveLevel.intValue()
    }

    actual open fun log(level: Level, msg: String) {
        if (isLoggable(level)) {
            println("[${level.getName()}] $nameStr: $msg")
        }
    }

    actual open fun log(level: Level, msg: String, thrown: Throwable?) {
        if (isLoggable(level)) {
            println("[${level.getName()}] $nameStr: $msg")
            thrown?.printStackTrace()
        }
    }

    actual open fun severe(msg: String) = log(Level.SEVERE, msg)
    actual open fun warning(msg: String) = log(Level.WARNING, msg)
    actual open fun info(msg: String) = log(Level.INFO, msg)
    actual open fun config(msg: String) = log(Level.CONFIG, msg)
    actual open fun fine(msg: String) = log(Level.FINE, msg)
    actual open fun finer(msg: String) = log(Level.FINER, msg)
    actual open fun finest(msg: String) = log(Level.FINEST, msg)

    actual companion object {
        actual val GLOBAL_LOGGER_NAME: String = "global"
        private val loggers = mutableMapOf<String, Logger>()
        private val globalLogger = Logger(GLOBAL_LOGGER_NAME)

        actual fun getLogger(name: String): Logger {
            return loggers.getOrPut(name) { Logger(name) }
        }

        actual fun getAnonymousLogger(): Logger = Logger("")

        actual fun getGlobal(): Logger = globalLogger
    }
}
