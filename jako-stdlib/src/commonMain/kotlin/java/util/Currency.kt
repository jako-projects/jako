@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable

expect class Currency : Serializable {
    fun getCurrencyCode(): String
    fun getSymbol(): String
    fun getDefaultFractionDigits(): Int

    val currencyCode: String
    val symbol: String
    val defaultFractionDigits: Int

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        // [Nullability Decision] Java getInstance(String) currencyCode is non-null; unknown codes throw IllegalArgumentException.
        fun getInstance(currencyCode: String): Currency
        fun getInstance(locale: Locale): Currency
    }
}
