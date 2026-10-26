package jako.test.util.logging

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoggingTest {

    @Test
    fun testLevelValues() {
        assertEquals("INFO", Level.INFO.getName())
        assertEquals("INFO", Level.INFO.name)
        assertEquals(800, Level.INFO.intValue())

        assertEquals(Level.WARNING, Level.parse("WARNING"))
        assertEquals(Level.SEVERE, Level.parse("SEVERE"))
    }

    @Test
    fun testLoggerLevels() {
        val logger = Logger.getLogger("test.logger")
        assertEquals("test.logger", logger.getName())
        assertEquals("test.logger", logger.name)

        logger.setLevel(Level.WARNING)
        assertEquals(Level.WARNING, logger.getLevel())

        assertTrue(logger.isLoggable(Level.SEVERE))
        assertTrue(logger.isLoggable(Level.WARNING))
        assertFalse(logger.isLoggable(Level.INFO))
        assertFalse(logger.isLoggable(Level.FINE))
    }

    @Test
    fun testLogRecordFormatterAndHandler() {
        val record = LogRecord(Level.INFO, "Hello logging")
        record.loggerName = "my.logger"
        record.millis = 1000L

        assertEquals(Level.INFO, record.level)
        assertEquals("Hello logging", record.message)
        assertEquals("my.logger", record.loggerName)
        assertEquals(1000L, record.millis)

        val formatter = SimpleFormatter()
        val formatted = formatter.format(record)
        assertTrue(formatted.contains("Hello logging"))
        assertTrue(formatted.contains("INFO"))

        val published = mutableListOf<String>()
        var flushed = false
        var closed = false
        val handler = object : Handler() {
            override fun publish(record: LogRecord) {
                if (record.level.intValue() >= getLevel().intValue()) {
                    published.add(record.message)
                }
            }

            override fun flush() {
                flushed = true
            }

            override fun close() {
                closed = true
            }
        }
        assertEquals(Level.ALL, handler.getLevel())
        handler.setLevel(Level.WARNING)
        assertEquals(Level.WARNING, handler.level)
        handler.publish(LogRecord(Level.INFO, "ignored"))
        handler.publish(LogRecord(Level.SEVERE, "captured"))
        handler.flush()
        handler.close()
        assertEquals(listOf("captured"), published)
        assertTrue(flushed)
        assertTrue(closed)
    }
}
