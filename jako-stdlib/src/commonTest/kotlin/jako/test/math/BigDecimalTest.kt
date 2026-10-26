package jako.test.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BigDecimalTest {

    @Test
    fun testMathContextConstantsAndConstructors() {
        val unlimited: MathContext = MathContext.UNLIMITED
        assertEquals(0, unlimited.precision)
        assertEquals(RoundingMode.HALF_UP, unlimited.roundingMode)
        assertEquals("precision=0 roundingMode=HALF_UP", unlimited.toString())

        val d32: MathContext = MathContext.DECIMAL32
        assertEquals(7, d32.precision)
        assertEquals(RoundingMode.HALF_EVEN, d32.roundingMode)
        assertEquals("precision=7 roundingMode=HALF_EVEN", d32.toString())

        val d64: MathContext = MathContext.DECIMAL64
        assertEquals(16, d64.precision)
        assertEquals(RoundingMode.HALF_EVEN, d64.roundingMode)

        val d128: MathContext = MathContext.DECIMAL128
        assertEquals(34, d128.precision)
        assertEquals(RoundingMode.HALF_EVEN, d128.roundingMode)

        val custom = MathContext(10)
        assertEquals(10, custom.precision)
        assertEquals(RoundingMode.HALF_UP, custom.roundingMode)

        val customFloor = MathContext(12, RoundingMode.FLOOR)
        assertEquals(12, customFloor.precision)
        assertEquals(RoundingMode.FLOOR, customFloor.roundingMode)

        // String constructor round-trip
        for (mc in listOf(unlimited, d32, d64, d128, custom, customFloor)) {
            val parsed = MathContext(mc.toString())
            assertEquals(mc, parsed)
            assertEquals(mc.hashCode(), parsed.hashCode())
        }
        assertNotEquals(d32, d64)

        // Invalid precision and string formats
        assertFailsWith<IllegalArgumentException> { MathContext(-1) }
        assertFailsWith<IllegalArgumentException> { MathContext(-5, RoundingMode.DOWN) }
        assertFailsWith<IllegalArgumentException> { MathContext("") }
        assertFailsWith<IllegalArgumentException> { MathContext("precision=-1 roundingMode=HALF_UP") }
        assertFailsWith<IllegalArgumentException> { MathContext("precision=abc roundingMode=HALF_UP") }
        assertFailsWith<IllegalArgumentException> { MathContext("precision=7 roundingMode=INVALID") }
        assertFailsWith<IllegalArgumentException> { MathContext("roundingMode=HALF_UP precision=7") }
    }

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
    fun testMathContextConstructorsAndArithmetic() {
        val mc3 = MathContext(3, RoundingMode.HALF_UP)
        assertEquals("123", BigDecimal("123.4", mc3).toPlainString())
        assertEquals("124", BigDecimal("123.5", mc3).toPlainString())
        assertEquals("1.23E+4", BigDecimal(12345, mc3).toString())
        assertEquals("1.23E+4", BigDecimal(12345L, mc3).toString())
        assertEquals("1.23E+5", BigDecimal(BigInteger("123456"), mc3).toString())
        assertEquals("12.3", BigDecimal(BigInteger("12345"), 3, mc3).toString())
        assertEquals("0.1000000", BigDecimal(0.1, MathContext.DECIMAL32).toPlainString())

        // Carry during rounding (9.99 -> 10 with precision 2)
        val mc2 = MathContext(2, RoundingMode.HALF_UP)
        val carried = BigDecimal("9.99", mc2)
        assertEquals("10", carried.toString())
        assertEquals(0, carried.scale())
        assertEquals(2, carried.precision())

        val x = BigDecimal("12.34")
        val y = BigDecimal("5.678")
        assertEquals("18.0", x.add(y, mc3).toString())
        assertEquals("6.66", x.subtract(y, mc3).toString())
        assertEquals("70.1", x.multiply(y, mc3).toString())
        assertEquals("2.17", x.divide(y, mc3).toString())
        assertEquals("12.3", x.negate().abs(mc3).toString())
        assertEquals("-12.3", x.negate(mc3).toString())
        assertEquals("12.3", x.plus(mc3).toString())
    }

    @Test
    fun testAllEightRoundingModes() {
        val values = listOf("2.5", "1.6", "1.1", "1.0", "-1.0", "-1.1", "-1.6", "-2.5")
        val expected = mapOf(
            RoundingMode.UP to listOf("3", "2", "2", "1", "-1", "-2", "-2", "-3"),
            RoundingMode.DOWN to listOf("2", "1", "1", "1", "-1", "-1", "-1", "-2"),
            RoundingMode.CEILING to listOf("3", "2", "2", "1", "-1", "-1", "-1", "-2"),
            RoundingMode.FLOOR to listOf("2", "1", "1", "1", "-1", "-2", "-2", "-3"),
            RoundingMode.HALF_UP to listOf("3", "2", "1", "1", "-1", "-1", "-2", "-3"),
            RoundingMode.HALF_DOWN to listOf("2", "2", "1", "1", "-1", "-1", "-2", "-2"),
            RoundingMode.HALF_EVEN to listOf("2", "2", "1", "1", "-1", "-1", "-2", "-2")
        )
        for ((mode, expList) in expected) {
            for (i in values.indices) {
                val bd = BigDecimal(values[i])
                assertEquals(expList[i], bd.setScale(0, mode).toPlainString(), "Mode $mode on ${values[i]}")
            }
        }
        assertEquals("1", BigDecimal("1.0").setScale(0, RoundingMode.UNNECESSARY).toPlainString())
        assertEquals("-1", BigDecimal("-1.0").setScale(0, RoundingMode.UNNECESSARY).toPlainString())
        assertFailsWith<ArithmeticException> {
            BigDecimal("1.1").setScale(0, RoundingMode.UNNECESSARY)
        }
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

        // Repeating decimal with MathContext
        assertEquals("0.3333333", one.divide(three, MathContext.DECIMAL32).toPlainString())
        assertEquals("0.3333333333333333", one.divide(three, MathContext.DECIMAL64).toPlainString())

        // Exact terminating decimal division without RoundingMode
        val eight = BigDecimal("8")
        val exact = one.divide(eight)
        assertEquals("0.125", exact.toPlainString())
        assertEquals(3, exact.scale())

        val exactMc = one.divide(eight, MathContext.DECIMAL32)
        assertEquals("0.125", exactMc.toPlainString())
        assertEquals(3, exactMc.scale())

        // Non-terminating division without RoundingMode must throw ArithmeticException
        assertFailsWith<ArithmeticException> {
            one.divide(three)
        }
        assertFailsWith<ArithmeticException> {
            one.divide(three, MathContext.UNLIMITED)
        }
        assertFailsWith<ArithmeticException> {
            one.divide(BigDecimal.ZERO, MathContext.DECIMAL32)
        }

        // divideToIntegralValue and divideAndRemainder
        val ten = BigDecimal("10")
        val qr = ten.divideAndRemainder(three)
        assertEquals("3", qr[0].toPlainString())
        assertEquals("1", qr[1].toPlainString())
        assertEquals("1", ten.remainder(three).toPlainString())
    }

    @Test
    fun testScaleAndStripZeros() {
        val d = BigDecimal("123.4500")
        assertEquals(4, d.scale())
        assertEquals(7, d.precision())

        val stripped = d.stripTrailingZeros()
        assertEquals("123.45", stripped.toPlainString())
        assertEquals(2, stripped.scale())

        val hundred = BigDecimal("100.00").stripTrailingZeros()
        assertEquals(-2, hundred.scale())
        assertEquals("1E+2", hundred.toString())
        assertEquals("100", hundred.toPlainString())

        val zeroStripped = BigDecimal("0.0000").stripTrailingZeros()
        assertEquals(0, zeroStripped.scale())
        assertEquals("0", zeroStripped.toString())
    }

    @Test
    fun testStringFormattingAndExactConversions() {
        assertEquals("0.000001", BigDecimal("1E-6").toString())
        assertEquals("1E-7", BigDecimal("1E-7").toString())
        assertEquals("0.0000001", BigDecimal("1E-7").toPlainString())
        assertEquals("100E-9", BigDecimal("1E-7").toEngineeringString())

        val bd = BigDecimal("42.75")
        assertEquals(42, bd.toInt())
        assertEquals(42L, bd.toLong())
        assertEquals(BigInteger.valueOf(42L), bd.toBigInteger())
        assertFailsWith<ArithmeticException> { bd.toBigIntegerExact() }
        assertFailsWith<ArithmeticException> { bd.intValueExact() }
        assertFailsWith<ArithmeticException> { bd.longValueExact() }

        val exactInt = BigDecimal("127.00")
        assertEquals(127.toByte(), exactInt.byteValueExact())
        assertEquals(127.toShort(), exactInt.shortValueExact())
        assertEquals(127, exactInt.intValueExact())
        assertEquals(127L, exactInt.longValueExact())

        assertFailsWith<ArithmeticException> { BigDecimal("128").byteValueExact() }
        assertFailsWith<ArithmeticException> { BigDecimal("32768").shortValueExact() }
        assertFailsWith<ArithmeticException> { BigDecimal("2147483648").intValueExact() }
    }

    @Test
    fun testComparisons() {
        val a = BigDecimal("1.0")
        val b = BigDecimal("1.00")
        assertEquals(0, a.compareTo(b))
        assertFalse(a == b)
        assertTrue(a <= b)
        assertTrue(a >= b)
        assertEquals(a, a.min(b))
        assertEquals(a, a.max(b))
    }

    @Test
    fun testPow() {
        val d = BigDecimal("1.5")
        val cubed = d.pow(3)
        assertEquals("3.375", cubed.toPlainString())
        assertEquals("3.38", d.pow(3, MathContext(3, RoundingMode.HALF_UP)).toPlainString())
        assertEquals("0.5", BigDecimal("2").pow(-1, MathContext.DECIMAL32).toPlainString())
        assertFailsWith<ArithmeticException> { d.pow(-1) }
    }

    @Test
    fun testDivideToIntegralValuePreferredScale() {
        // preferredScale = 2 - 0 = 2 -> must preserve trailing zeros ("3.00", scale 2)
        val q1 = BigDecimal("10.00").divideToIntegralValue(BigDecimal("3"))
        assertEquals(2, q1.scale())
        assertEquals("3.00", q1.toPlainString())

        // With MathContext(2), 1 digit quotient '3' can only be padded with 1 zero -> "3.0" (scale 1)
        val q2 = BigDecimal("10.00").divideToIntegralValue(BigDecimal("3"), MathContext(2))
        assertEquals(1, q2.scale())
        assertEquals("3.0", q2.toPlainString())

        // Quotient requires 2 digits ("33"), so MathContext(1) throws ArithmeticException
        assertFailsWith<ArithmeticException> {
            BigDecimal("100").divideToIntegralValue(BigDecimal("3"), MathContext(1))
        }
    }
}

