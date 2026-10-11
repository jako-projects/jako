@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class Locale : Serializable, Cloneable {
    constructor(language: String, country: String, variant: String)
    constructor(language: String, country: String)
    constructor(language: String)

    open fun getLanguage(): String
    open fun getCountry(): String
    open fun getVariant(): String
    open fun getDisplayName(): String
    open fun getDisplayLanguage(): String
    open fun getDisplayCountry(): String
    open fun toLanguageTag(): String

    open val language: String
    open val country: String
    open val variant: String
    open val displayName: String
    open val displayLanguage: String
    open val displayCountry: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        val ENGLISH: Locale
        val FRENCH: Locale
        val GERMAN: Locale
        val ITALIAN: Locale
        val JAPANESE: Locale
        val KOREAN: Locale
        val CHINESE: Locale
        val SIMPLIFIED_CHINESE: Locale
        val TRADITIONAL_CHINESE: Locale
        val FRANCE: Locale
        val GERMANY: Locale
        val ITALY: Locale
        val JAPAN: Locale
        val KOREA: Locale
        val CHINA: Locale
        val PRC: Locale
        val TAIWAN: Locale
        val UK: Locale
        val US: Locale
        val CANADA: Locale
        val CANADA_FRENCH: Locale
        val ROOT: Locale
        val DEFAULT: Locale

        fun getDefault(): Locale
        fun setDefault(newLocale: Locale)
        fun forLanguageTag(languageTag: String): Locale
    }
}
