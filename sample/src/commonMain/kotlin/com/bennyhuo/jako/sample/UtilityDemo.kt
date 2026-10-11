package com.bennyhuo.jako.sample

import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

object UtilityDemo {

    data class UtilityReport(
        val formattedInvoice: String,
        val sha256Base64: String,
        val optionalValue: String?
    )

    fun run(): UtilityReport {
        // 1. java.math.BigDecimal with Kotlin operator overloads (+, *)
        val unitPrice = BigDecimal("19.95")
        val quantity = BigDecimal("3")
        val taxRate = BigDecimal("1.08")
        val total = (unitPrice * quantity * taxRate).setScale(2, RoundingMode.HALF_UP)

        // 2. java.time.LocalDateTime & DateTimeFormatter
        val timestamp = LocalDateTime.of(2026, 10, 10, 12, 30, 0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // 3. String.format with java.util.Locale
        val invoice = String.format(Locale.US, "[%s] Total: $%s", timestamp, total.toPlainString())

        // 4. java.security.MessageDigest (SHA-256) + java.util.Base64
        val digest = MessageDigest.getInstance("SHA-256").digest(invoice.toByteArray())
        val encoded = Base64.getEncoder().encodeToString(digest)

        // 5. java.util.Optional + kotlin.jvm.optionals.getOrNull
        val opt = Optional.ofNullable("verified")

        return UtilityReport(
            formattedInvoice = invoice,
            sha256Base64 = encoded,
            optionalValue = opt.getOrNull()
        )
    }
}
