@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.logging

expect open class Logger {
    open fun getName(): String
    open val name: String
    open fun getLevel(): Level?
    open fun setLevel(newLevel: Level?)
    open fun isLoggable(level: Level): Boolean
    open fun log(level: Level, msg: String)
    /** [Nullability Decision] Java Logger.log(Level,String,Throwable) accepts a null thrown. */
    open fun log(level: Level, msg: String, thrown: Throwable?)
    open fun severe(msg: String)
    open fun warning(msg: String)
    open fun info(msg: String)
    open fun config(msg: String)
    open fun fine(msg: String)
    open fun finer(msg: String)
    open fun finest(msg: String)

    companion object {
        val GLOBAL_LOGGER_NAME: String
        fun getLogger(name: String): Logger
        fun getAnonymousLogger(): Logger
        fun getGlobal(): Logger
    }
}
