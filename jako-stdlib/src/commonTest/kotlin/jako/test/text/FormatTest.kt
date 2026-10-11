package jako.test.text

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {

    @Test
    fun testDecimalFormat() {
        val df = DecimalFormat("#,##0.00")
        val formatted = df.format(1234567.89)
        assertEquals("1,234,567.89", formatted)

        val pos = ParsePosition(0)
        val parsed = df.parse("1,234,567.89", pos)
        assertEquals(1234567.89, parsed?.toDouble())
    }

    @Test
    fun testNumberFormat() {
        val nf = NumberFormat.getIntegerInstance()
        val formatted = nf.format(123456)
        assertEquals("123,456", formatted)
    }
}
