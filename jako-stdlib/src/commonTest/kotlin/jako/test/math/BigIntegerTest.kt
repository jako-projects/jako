package jako.test.math

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BigIntegerTest {

    @Test
    fun testBasicArithmetic() {
        val a = BigInteger("12345678901234567890")
        val b = BigInteger("98765432109876543210")

        val sum = a + b
        assertEquals("111111111011111111100", sum.toString())

        val diff = b - a
        assertEquals("86419753208641975320", diff.toString())

        val prod = a * BigInteger.valueOf(2L)
        assertEquals("24691357802469135780", prod.toString())

        val div = b / a
        assertEquals(8L, div.toLong())

        val rem = b % a
        assertEquals(b, div * a + rem)
    }

    @Test
    fun testPowAndGcd() {
        val two = BigInteger.valueOf(2L)
        val power = two.pow(10)
        assertEquals("1024", power.toString())

        val gcd = BigInteger.valueOf(48L).gcd(BigInteger.valueOf(18L))
        assertEquals(6L, gcd.toLong())
    }

    @Test
    fun testBitwiseOperations() {
        val a = BigInteger("12") // 1100
        val b = BigInteger("10") // 1010

        assertEquals(8L, (a and b).toLong()) // 1000 = 8
        assertEquals(14L, (a or b).toLong())  // 1110 = 14
        assertEquals(6L, (a xor b).toLong())  // 0110 = 6

        val shifted = a shl 2
        assertEquals(48L, shifted.toLong())

        assertTrue(a.testBit(2))
        assertTrue(a.testBit(3))
        assertFalse(a.testBit(0))
    }

    @Test
    fun testByteArrayConversions() {
        val num = BigInteger("1000000")
        val bytes = num.toByteArray()
        val reconstructed = BigInteger(1, bytes)
        assertEquals(num, reconstructed)
    }

    @Test
    fun testRadixConversions() {
        val num = BigInteger("ff", 16)
        assertEquals(255L, num.toLong())
        assertEquals("11111111", num.toString(2))
        assertEquals("ff", num.toString(16))
    }
}
