package com.bennyhuo.jako.compiler

import com.bennyhuo.jako.compiler.parcelize.JakoParcelizeIrGenerationExtension
import com.bennyhuo.jako.compiler.synchronized.JakoSynchronizedIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.parcelize.ParcelizeNames
import org.jetbrains.kotlin.parcelize.fir.FirParcelizeExtensionRegistrar

class JakoCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = JAKO_COMPILER_PLUGIN_ID
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(
            FirParcelizeExtensionRegistrar(ParcelizeNames.PARCELIZE_CLASS_FQ_NAMES)
        )
        FirExtensionRegistrarAdapter.registerExtension(JakoFirExtensionRegistrar())
        IrGenerationExtension.registerExtension(JakoSynchronizedIrGenerationExtension())
        IrGenerationExtension.registerExtension(JakoParcelizeIrGenerationExtension())
    }
}
