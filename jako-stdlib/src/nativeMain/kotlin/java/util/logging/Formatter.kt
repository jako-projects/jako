package java.util.logging

actual abstract class Formatter protected actual constructor() {
    actual abstract fun format(record: LogRecord): String
    actual open fun formatMessage(record: LogRecord): String = record.getMessage()
}
