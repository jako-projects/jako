package com.bennyhuo.jako.compiler.websupport

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.getSingleMatchedExpectForActualOrNull
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirCollectionLiteral
import org.jetbrains.kotlin.fir.expressions.FirEnumEntryDeserializedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFileSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.wasm.WasmTarget

class JakoWebSupportFirChecker(
    session: FirSession,
    private val configuredWebRuntimes: Set<String>? = null,
    private val severity: String = "warning"
) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirFunctionCallChecker> = setOf(FunctionCallChecker())
        override val propertyAccessExpressionCheckers: Set<FirPropertyAccessExpressionChecker> =
            setOf(PropertyAccessChecker())
    }

    private inner class FunctionCallChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirFunctionCall) {
            checkQualifiedAccess(expression, context, reporter)
        }
    }

    private inner class PropertyAccessChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirPropertyAccessExpression) {
            checkQualifiedAccess(expression, context, reporter)
        }
    }

    private data class WebSupportMeta(
        val runtimes: Set<String>,
        val reason: String,
        val alternative: String
    )

    companion object {
        private val WEB_SUPPORT_CLASS_ID = ClassId(
            FqName("com.bennyhuo.jako.annotations"),
            Name.identifier("WebSupport")
        )
        private val SUPPRESS_CLASS_ID = ClassId(
            FqName("kotlin"),
            Name.identifier("Suppress")
        )

        private val INTERNAL_LIBRARY_PACKAGE_PREFIXES = listOf(
            "java.",
            "javax.",
            "kotlin.",
            "android.",
            "org.json",
            "org.xmlpull",
            "jako.test",
            "jako.android.test"
        )
    }

    private fun resolveRequiredWebRuntimes(): Set<String> {
        if (severity == "none" || severity == "ignore") return emptySet()
        if (configuredWebRuntimes != null) {
            return configuredWebRuntimes
        }
        val platform = session.moduleData.platform
        if (platform.isWasm()) {
            val isWasi = platform.componentPlatforms.any { comp ->
                comp.toString().contains("WASI", ignoreCase = true) ||
                    (comp is org.jetbrains.kotlin.platform.wasm.WasmPlatformWithTarget && comp.target == WasmTarget.WASI)
            }
            return if (isWasi) setOf("WASI") else setOf("NODE_JS")
        }
        if (platform.isJs()) {
            return setOf("NODE_JS")
        }
        return emptySet()
    }

    private fun checkQualifiedAccess(
        expression: FirQualifiedAccessExpression,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val requiredRuntimes = resolveRequiredWebRuntimes()
        if (requiredRuntimes.isEmpty()) return

        // Skip internal stdlib/android implementation and internal parity test packages unless explicitly configured
        if (!shouldCheckPackage(context, configuredWebRuntimes != null)) return

        val callableSymbol = expression.calleeReference.toResolvedCallableSymbol() ?: return
        val meta = findWebSupportMeta(callableSymbol) ?: return

        // Check if enclosing declarations suppress UNSUPPORTED_WEB_API or already restrict to compatible @WebSupport
        if (isSuppressedOrPropagated(context, meta.runtimes)) return

        val missingRuntimes = if (meta.runtimes.isEmpty()) {
            requiredRuntimes
        } else {
            requiredRuntimes - meta.runtimes
        }
        if (missingRuntimes.isEmpty()) return

        val apiDisplayName = buildApiDisplayName(callableSymbol)
        val supportedDesc = if (meta.runtimes.isEmpty()) {
            "none (JVM/Android/Native only)"
        } else {
            meta.runtimes.joinToString(", ")
        }
        val reasonSuffix = if (meta.reason.isNotBlank()) ": ${meta.reason}." else "."
        val altSuffix = if (meta.alternative.isNotBlank()) " Recommended alternative: ${meta.alternative}." else ""
        val message = "[jako] API '$apiDisplayName' is not supported on WebRuntime ${missingRuntimes.joinToString("/")} " +
            "(supported WebRuntimes: $supportedDesc)$reasonSuffix$altSuffix"

        val factory = if (severity == "error") {
            JakoWebSupportErrors.UNSUPPORTED_WEB_API_ERROR
        } else {
            JakoWebSupportErrors.UNSUPPORTED_WEB_API
        }
        reporter.reportOn(expression.source, factory, message, context)
    }

    private fun shouldCheckPackage(context: CheckerContext, explicitlyConfigured: Boolean): Boolean {
        if (explicitlyConfigured) return true
        val pkg = context.containingDeclarations.firstNotNullOfOrNull { sym ->
            when (sym) {
                is FirFileSymbol -> sym.fir.packageFqName.asString()
                is FirClassLikeSymbol<*> -> sym.classId.packageFqName.asString()
                is FirCallableSymbol<*> -> sym.callableId?.packageName?.asString()
                else -> null
            }
        } ?: ""
        if (pkg == "com.bennyhuo.jako" || pkg.startsWith("com.bennyhuo.jako.annotations")) {
            return false
        }
        if (INTERNAL_LIBRARY_PACKAGE_PREFIXES.any { pkg == it.removeSuffix(".") || pkg.startsWith(it) }) {
            return false
        }
        return true
    }

    private fun isSuppressedOrPropagated(
        context: CheckerContext,
        calleeSupportedRuntimes: Set<String>
    ): Boolean {
        for (sym in context.containingDeclarations) {
            val annotations = sym.resolvedAnnotationsWithArguments.ifEmpty { sym.annotations }
            for (ann in annotations) {
                if (isSuppressAnnotation(ann)) {
                    val names = extractStringVarargs(ann)
                    if ("UNSUPPORTED_WEB_API" in names || "UNSUPPORTED_WEB_API_ERROR" in names) {
                        return true
                    }
                }
                if (isWebSupportAnnotation(ann)) {
                    val callerMeta = parseWebSupportAnnotation(ann)
                    if (callerMeta.runtimes.isNotEmpty() && calleeSupportedRuntimes.containsAll(callerMeta.runtimes)) {
                        return true
                    }
                    if (callerMeta.runtimes.isEmpty() && calleeSupportedRuntimes.isEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun findWebSupportMeta(callableSymbol: FirCallableSymbol<*>): WebSupportMeta? {
        extractWebSupportFromSymbol(callableSymbol)?.let { return it }

        val cid = callableSymbol.callableId ?: return null
        val classId = cid.classId ?: return null
        val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
        if (classSymbol != null) {
            extractWebSupportFromSymbol(classSymbol)?.let { return it }
        }
        val outerClassId = classId.outerClassId
        if (outerClassId != null) {
            val outerSymbol = session.symbolProvider.getClassLikeSymbolByClassId(outerClassId)
            if (outerSymbol != null) {
                extractWebSupportFromSymbol(outerSymbol)?.let { return it }
            }
        }
        return null
    }

    private fun extractWebSupportFromSymbol(symbol: FirBasedSymbol<*>): WebSupportMeta? {
        val direct = symbol.resolvedAnnotationsWithArguments.firstOrNull { isWebSupportAnnotation(it) }
            ?: symbol.annotations.firstOrNull { isWebSupportAnnotation(it) }
        if (direct != null) return parseWebSupportAnnotation(direct)

        val expectSymbol = symbol.getSingleMatchedExpectForActualOrNull()
        if (expectSymbol != null) {
            val fromExpect = expectSymbol.resolvedAnnotationsWithArguments.firstOrNull { isWebSupportAnnotation(it) }
                ?: expectSymbol.annotations.firstOrNull { isWebSupportAnnotation(it) }
            if (fromExpect != null) return parseWebSupportAnnotation(fromExpect)
        }
        return null
    }

    private fun isWebSupportAnnotation(ann: FirAnnotation): Boolean {
        val cid = ann.toAnnotationClassId(session)
        return cid == WEB_SUPPORT_CLASS_ID || cid?.shortClassName?.asString() == "WebSupport"
    }

    private fun isSuppressAnnotation(ann: FirAnnotation): Boolean {
        val cid = ann.toAnnotationClassId(session)
        return cid == SUPPRESS_CLASS_ID || cid?.shortClassName?.asString() == "Suppress"
    }

    private fun parseWebSupportAnnotation(ann: FirAnnotation): WebSupportMeta {
        val runtimes = LinkedHashSet<String>()
        var reason = ""
        var alternative = ""

        for ((name, expr) in ann.argumentMapping.mapping) {
            when (name.asString()) {
                "runtimes" -> extractEnumNames(expr, runtimes)
                "reason" -> reason = extractStringLiteral(expr) ?: ""
                "alternative" -> alternative = extractStringLiteral(expr) ?: ""
            }
        }
        return WebSupportMeta(runtimes, reason, alternative)
    }

    private fun extractEnumNames(expr: FirExpression, out: MutableSet<String>) {
        when (expr) {
            is FirVarargArgumentsExpression -> {
                for (arg in expr.arguments) {
                    extractEnumNames(arg, out)
                }
            }
            is FirCollectionLiteral -> {
                for (arg in expr.argumentList.arguments) {
                    extractEnumNames(arg, out)
                }
            }
            is FirEnumEntryDeserializedAccessExpression -> {
                out.add(expr.enumEntryName.asString())
            }
            is FirQualifiedAccessExpression -> {
                val resolvedName = expr.calleeReference.toResolvedCallableSymbol()?.callableId?.callableName?.asString()
                    ?: (expr.calleeReference as? FirNamedReference)?.name?.asString()
                if (!resolvedName.isNullOrEmpty()) {
                    out.add(resolvedName)
                }
            }
            else -> {}
        }
    }

    private fun extractStringVarargs(ann: FirAnnotation): Set<String> {
        val out = LinkedHashSet<String>()
        for ((_, expr) in ann.argumentMapping.mapping) {
            extractStrings(expr, out)
        }
        return out
    }

    private fun extractStrings(expr: FirExpression, out: MutableSet<String>) {
        when (expr) {
            is FirVarargArgumentsExpression -> expr.arguments.forEach { extractStrings(it, out) }
            is FirCollectionLiteral -> expr.argumentList.arguments.forEach { extractStrings(it, out) }
            else -> extractStringLiteral(expr)?.let { out.add(it) }
        }
    }

    private fun extractStringLiteral(expr: FirExpression): String? {
        return (expr as? FirLiteralExpression)?.value as? String
    }

    private fun buildApiDisplayName(symbol: FirCallableSymbol<*>): String {
        val cid = symbol.callableId ?: return symbol.toString()
        val classPart = cid.classId?.let { clsId ->
            val outer = clsId.outerClassId
            if (outer != null && clsId.shortClassName.asString() == "Companion") {
                outer.asSingleFqName().asString()
            } else {
                clsId.asSingleFqName().asString()
            }
        } ?: cid.packageName.asString()
        val memberName = cid.callableName.asString()
        val shortClassName = cid.classId?.shortClassName?.asString()
        return if (memberName == "<init>" || memberName == shortClassName) {
            classPart
        } else if (classPart.isNotEmpty()) {
            "$classPart.$memberName"
        } else {
            memberName
        }
    }
}
