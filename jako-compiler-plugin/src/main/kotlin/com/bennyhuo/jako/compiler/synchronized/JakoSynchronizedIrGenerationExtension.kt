package com.bennyhuo.jako.compiler.synchronized

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.isJvm

class JakoSynchronizedIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // On JVM, the standard Kotlin backend already emits ACC_SYNCHRONIZED for @Synchronized.
        if (pluginContext.platform.isJvm()) return

        val lockSymbol = pluginContext.referenceFunctions(LOCK_MONITOR_CALLABLE_ID).firstOrNull() ?: return
        val unlockSymbol = pluginContext.referenceFunctions(UNLOCK_MONITOR_CALLABLE_ID).firstOrNull() ?: return

        moduleFragment.transformChildrenVoid(
            SynchronizedFunctionTransformer(pluginContext, lockSymbol, unlockSymbol)
        )
    }

    companion object {
        private val JAVA_LANG_PACKAGE = FqName("java.lang")
        private val LOCK_MONITOR_CALLABLE_ID = CallableId(JAVA_LANG_PACKAGE, Name.identifier("lockObjectMonitor"))
        private val UNLOCK_MONITOR_CALLABLE_ID = CallableId(JAVA_LANG_PACKAGE, Name.identifier("unlockObjectMonitor"))
    }
}
