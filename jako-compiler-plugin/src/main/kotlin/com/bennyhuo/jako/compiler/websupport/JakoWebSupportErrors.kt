package com.bennyhuo.jako.compiler.websupport

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.warning1

object JakoWebSupportErrors : KtDiagnosticsContainer() {
    val UNSUPPORTED_WEB_API by warning1<PsiElement, String>(SourceElementPositioningStrategies.DEFAULT)
    val UNSUPPORTED_WEB_API_ERROR by error1<PsiElement, String>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RendererFactory

    private object RendererFactory : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap("JakoWebSupport") { map ->
            map.put(UNSUPPORTED_WEB_API, "{0}", CommonRenderers.STRING)
            map.put(UNSUPPORTED_WEB_API_ERROR, "{0}", CommonRenderers.STRING)
        }
    }
}
