package jako.test.util

import java.util.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyTest {

    @Test
    fun testGetInstanceUsd() {
        val usd = Currency.getInstance("USD")
        assertEquals("USD", usd.currencyCode)
        assertEquals("USD", usd.getCurrencyCode())
        assertEquals(2, usd.defaultFractionDigits)
    }

    @Test
    fun testIsoCurrencies() {
        assertEquals("EUR", Currency.getInstance("EUR").currencyCode)
        assertEquals("CNY", Currency.getInstance("CNY").currencyCode)
        assertEquals("JPY", Currency.getInstance("JPY").currencyCode)
        assertEquals(0, Currency.getInstance("JPY").defaultFractionDigits)
        assertEquals("USD", Currency.getInstance("USD").toString())
    }

    @Test
    fun testUnknownCurrency() {
        assertFailsWith<IllegalArgumentException> {
            Currency.getInstance("usd")
        }
        assertFailsWith<IllegalArgumentException> {
            Currency.getInstance("ZZZ")
        }
    }
}
