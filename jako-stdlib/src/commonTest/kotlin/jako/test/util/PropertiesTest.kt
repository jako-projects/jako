package jako.test.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertiesTest {

    @Test
    fun testBasicProperties() {
        val props = Properties()
        props.setProperty("name", "jako")
        props.setProperty("version", "0.1.0")

        assertEquals("jako", props.getProperty("name"))
        assertEquals("0.1.0", props.getProperty("version"))
        assertNull(props.getProperty("non_existent"))
        assertEquals("default_val", props.getProperty("non_existent", "default_val"))

        assertTrue(props.stringPropertyNames().contains("name"))
        assertTrue(props.stringPropertyNames().contains("version"))
    }

    @Test
    fun testPropertiesWithDefaults() {
        val defaults = Properties()
        defaults.setProperty("timeout", "3000")

        val props = Properties(defaults)
        props.setProperty("name", "custom")

        assertEquals("3000", props.getProperty("timeout"))
        assertEquals("custom", props.getProperty("name"))
    }

    @Test
    fun testLoadAndStore() {
        val raw = """
            # Configuration
            app.name=JakoApp
            app.port: 8080
            app.desc = Fast & Native
        """.trimIndent()

        val props = Properties()
        props.load(ByteArrayInputStream(raw.encodeToByteArray()))

        assertEquals("JakoApp", props.getProperty("app.name"))
        assertEquals("8080", props.getProperty("app.port"))
        assertEquals("Fast & Native", props.getProperty("app.desc"))

        val out = ByteArrayOutputStream()
        props.store(out, "Generated")
        val savedText = out.toByteArray().decodeToString()
        assertTrue(savedText.contains("app.name=JakoApp"))
    }
}
