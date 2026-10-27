package java.util

import java.io.Serializable

actual class Currency private constructor(
    private val code: String,
    private val currencySymbol: String,
    private val fractionDigits: Int
) : Serializable {
    actual fun getCurrencyCode(): String = code
    actual fun getSymbol(): String = currencySymbol
    actual fun getDefaultFractionDigits(): Int = fractionDigits

    actual val currencyCode: String get() = getCurrencyCode()
    actual val symbol: String get() = getSymbol()
    actual val defaultFractionDigits: Int get() = getDefaultFractionDigits()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Currency) return false
        return code == other.code
    }

    actual override fun hashCode(): Int = code.hashCode()

    actual override fun toString(): String = code

    actual companion object {
        private val byCode: Map<String, Currency> = listOf(
            Currency("USD", "$", 2),
            Currency("EUR", "€", 2),
            Currency("CNY", "¥", 2),
            Currency("JPY", "¥", 0),
            Currency("GBP", "£", 2),
            Currency("KRW", "₩", 0),
            Currency("HKD", "HK$", 2),
            Currency("TWD", "NT$", 2),
            Currency("AUD", "A$", 2),
            Currency("CAD", "CA$", 2),
            Currency("CHF", "CHF", 2),
            Currency("SGD", "S$", 2),
            Currency("INR", "₹", 2),
            Currency("BRL", "R$", 2),
            Currency("RUB", "₽", 2),
            Currency("MXN", "MX$", 2),
            Currency("NZD", "NZ$", 2),
            Currency("SEK", "kr", 2),
            Currency("NOK", "kr", 2),
            Currency("DKK", "kr", 2),
            Currency("PLN", "zł", 2),
            Currency("TRY", "₺", 2),
            Currency("ZAR", "R", 2),
            Currency("THB", "฿", 2),
            Currency("IDR", "Rp", 2),
            Currency("MYR", "RM", 2),
            Currency("PHP", "₱", 2),
            Currency("VND", "₫", 0),
            Currency("AED", "AED", 2),
            Currency("SAR", "SAR", 2),
            Currency("ILS", "₪", 2),
            Currency("CZK", "Kč", 2),
            Currency("HUF", "Ft", 2)
        ).associateBy { it.code }

        private val byCountry: Map<String, String> = mapOf(
            "US" to "USD",
            "CA" to "CAD",
            "GB" to "GBP",
            "JP" to "JPY",
            "CN" to "CNY",
            "TW" to "TWD",
            "HK" to "HKD",
            "KR" to "KRW",
            "AU" to "AUD",
            "NZ" to "NZD",
            "IN" to "INR",
            "BR" to "BRL",
            "RU" to "RUB",
            "CH" to "CHF",
            "SG" to "SGD",
            "MX" to "MXN",
            "FR" to "EUR",
            "DE" to "EUR",
            "IT" to "EUR",
            "ES" to "EUR",
            "NL" to "EUR",
            "BE" to "EUR",
            "AT" to "EUR",
            "IE" to "EUR",
            "PT" to "EUR",
            "FI" to "EUR",
            "GR" to "EUR",
            "SE" to "SEK",
            "NO" to "NOK",
            "DK" to "DKK",
            "PL" to "PLN",
            "TR" to "TRY",
            "ZA" to "ZAR",
            "TH" to "THB"
        )

        actual fun getInstance(currencyCode: String): Currency {
            return byCode[currencyCode]
                ?: throw IllegalArgumentException(currencyCode)
        }

        actual fun getInstance(locale: Locale): Currency {
            val country = locale.getCountry()
            val code = byCountry[country]
                ?: throw IllegalArgumentException(locale.toString())
            return getInstance(code)
        }
    }
}
