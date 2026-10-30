package com.bennyhuo.jako.compiler

import com.bennyhuo.jako.compiler.parcelize.JakoParcelizeFirExtension
import com.bennyhuo.jako.compiler.synchronized.JakoSynchronizedFirStatusHook
import com.bennyhuo.jako.compiler.websupport.JakoWebSupportFirChecker
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class JakoFirExtensionRegistrar(
    private val configuredWebRuntimes: Set<String>? = null,
    private val webSupportSeverity: String = "warning"
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::JakoSynchronizedFirStatusHook
        +::JakoParcelizeFirExtension
        +{ session: FirSession ->
            JakoWebSupportFirChecker(
                session = session,
                configuredWebRuntimes = configuredWebRuntimes,
                severity = webSupportSeverity
            )
        }
    }
}
