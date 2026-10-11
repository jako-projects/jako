package java.util.logging

actual open class SimpleFormatter actual constructor() : Formatter() {
    actual override fun format(record: LogRecord): String {
        return "[${record.getLevel().getName()}] ${record.getMessage()}\n"
    }
}
