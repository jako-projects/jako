package java.util

import java.io.Serializable

actual open class Locale : Serializable, Cloneable {
    private val _language: String
    private val _country: String
    private val _variant: String

    actual constructor(language: String, country: String, variant: String) {
        this._language = language.lowercase()
        this._country = country.uppercase()
        this._variant = variant
    }

    actual constructor(language: String, country: String) : this(language, country, "")

    actual constructor(language: String) : this(language, "", "")

    actual open fun getLanguage(): String = _language
    actual open fun getCountry(): String = _country
    actual open fun getVariant(): String = _variant

    actual open val language: String get() = _language
    actual open val country: String get() = _country
    actual open val variant: String get() = _variant

    actual open fun getDisplayName(): String {
        val sb = StringBuilder()
        if (_language.isNotEmpty()) sb.append(_language)
        if (_country.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append(" (")
            sb.append(_country)
            if (_variant.isNotEmpty()) {
                sb.append(", ")
                sb.append(_variant)
            }
            sb.append(")")
        } else if (_variant.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append(" (")
            sb.append(_variant)
            sb.append(")")
        }
        return sb.toString()
    }

    actual open fun getDisplayLanguage(): String = _language
    actual open fun getDisplayCountry(): String = _country

    actual open val displayName: String get() = getDisplayName()
    actual open val displayLanguage: String get() = getDisplayLanguage()
    actual open val displayCountry: String get() = getDisplayCountry()

    actual open fun toLanguageTag(): String {
        val sb = StringBuilder()
        if (_language.isNotEmpty()) sb.append(_language) else sb.append("und")
        if (_country.isNotEmpty()) sb.append("-").append(_country)
        if (_variant.isNotEmpty()) sb.append("-").append(_variant)
        return sb.toString()
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Locale) return false
        return _language == other._language && _country == other._country && _variant == other._variant
    }

    actual override fun hashCode(): Int {
        var h = _language.hashCode()
        h = 31 * h + _country.hashCode()
        h = 31 * h + _variant.hashCode()
        return h
    }

    actual override fun toString(): String {
        val sb = StringBuilder(_language)
        if (_country.isNotEmpty() || _variant.isNotEmpty()) {
            sb.append("_").append(_country)
        }
        if (_variant.isNotEmpty()) {
            sb.append("_").append(_variant)
        }
        return sb.toString()
    }

    actual companion object {
        actual val ENGLISH: Locale = Locale("en", "")
        actual val FRENCH: Locale = Locale("fr", "")
        actual val GERMAN: Locale = Locale("de", "")
        actual val ITALIAN: Locale = Locale("it", "")
        actual val JAPANESE: Locale = Locale("ja", "")
        actual val KOREAN: Locale = Locale("ko", "")
        actual val CHINESE: Locale = Locale("zh", "")
        actual val SIMPLIFIED_CHINESE: Locale = Locale("zh", "CN")
        actual val TRADITIONAL_CHINESE: Locale = Locale("zh", "TW")
        actual val FRANCE: Locale = Locale("fr", "FR")
        actual val GERMANY: Locale = Locale("de", "DE")
        actual val ITALY: Locale = Locale("it", "IT")
        actual val JAPAN: Locale = Locale("ja", "JP")
        actual val KOREA: Locale = Locale("ko", "KR")
        actual val CHINA: Locale = SIMPLIFIED_CHINESE
        actual val PRC: Locale = SIMPLIFIED_CHINESE
        actual val TAIWAN: Locale = TRADITIONAL_CHINESE
        actual val UK: Locale = Locale("en", "GB")
        actual val US: Locale = Locale("en", "US")
        actual val CANADA: Locale = Locale("en", "CA")
        actual val CANADA_FRENCH: Locale = Locale("fr", "CA")
        actual val ROOT: Locale = Locale("", "")

        private var defaultLocale: Locale = US

        actual val DEFAULT: Locale get() = defaultLocale

        actual fun getDefault(): Locale = defaultLocale

        actual fun setDefault(newLocale: Locale) {
            defaultLocale = newLocale
        }

        actual fun forLanguageTag(languageTag: String): Locale {
            val parts = languageTag.split("-", "_")
            val lang = if (parts.isNotEmpty()) parts[0] else ""
            val country = if (parts.size > 1) parts[1] else ""
            val variant = if (parts.size > 2) parts.subList(2, parts.size).joinToString("_") else ""
            return Locale(lang, country, variant)
        }
    }
}
