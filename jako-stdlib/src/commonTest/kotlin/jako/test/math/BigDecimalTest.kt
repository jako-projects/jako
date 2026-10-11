package jako.test.math

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BigDecimalTest {

    @Test
    fun testBasicArithmetic() {
        val a = BigDecimal("10.50")
        val b = BigDecimal("3.25")

        val sum = a + b
        assertEquals("13.75", sum.toPlainString())

        val diff = a - b
        assertEquals("7.25", diff.toPlainString())

        val prod = a * b
        assertEquals("34.1250", prod.toPlainString())
    }

    @Test
    fun testDivisionAndRounding() {
        val a = BigDecimal("10.00")
        val b = BigDecimal("3.00")

        val divHalfUp = a.divide(b, 2, RoundingMode.HALF_UP)
        assertEquals("3.33", divHalfUp.toPlainString())

        val one = BigDecimal("1")
        val three = BigDecimal("3")
        val div4 = one.divide(three, 4, RoundingMode.HALF_UP)
        assertEquals("0.3333", div4.toPlainString())
    }

    @Test
    fun testScaleAndStripZeros() {
        val d = BigDecimal("123.4500")
        assertEquals(4, d.scale())
        assertEquals(7, d.precision())

        val stripped = d.stripTrailingZeros()
        assertEquals("123.45", stripped.toPlainString())
        assertEquals(2, stripped.scale())
    }

    @Test
    fun testComparisons() {
        val a = BigDecimal("1.0")
        val b = BigDecimal("1.00")
        assertEquals(0, a.compareTo(b))
        assertTrue(a <= b)
        assertTrue(a >= b)
    }

    @Test
    fun testPow() {
        val d = BigDecimal("1.5")
        val cubed = d.pow(3)
        assertEquals("3.375", cubed.toPlainString())
    }
}
