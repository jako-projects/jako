package com.bennyhuo.jako.compiler

import com.bennyhuo.jako.compiler.parcelize.JakoParcelizeFirExtension
import com.bennyhuo.jako.compiler.synchronized.JakoSynchronizedFirStatusHook
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class JakoFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::JakoSynchronizedFirStatusHook
        +::JakoParcelizeFirExtension
    }
}
