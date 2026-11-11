package com.bennyhuo.jako.compiler.synchronized

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.isJvm

class JakoSynchronizedIrGenerationExtension(
    private val messageCollector: MessageCollector = MessageCollector.NONE
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // On JVM, the standard Kotlin backend already emits ACC_SYNCHRONIZED for @Synchronized.
        if (pluginContext.platform.isJvm()) return

        val lockSymbol = pluginContext.referenceFunctions(LOCK_MONITOR_CALLABLE_ID).firstOrNull()
        val unlockSymbol = pluginContext.referenceFunctions(UNLOCK_MONITOR_CALLABLE_ID).firstOrNull()
        if (lockSymbol == null || unlockSymbol == null) {
            if (containsSynchronizedFunction(moduleFragment)) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "[jako] @Synchronized function(s) found on non-JVM target (${pluginContext.platform}), " +
                        "but 'com.bennyhuo.jako:jako-stdlib' is missing from dependencies; @Synchronized will not acquire monitor locks."
                )
            }
            return
        }
        val jakoSynchronizedSymbol = pluginContext.referenceFunctions(JAKO_SYNCHRONIZED_CALLABLE_ID).firstOrNull()
            ?: pluginContext.referenceFunctions(SYNCHRONIZED_CALLABLE_ID)
                .firstOrNull { !it.owner.hasAnnotation(DEPRECATED_FQ_NAME) }

        moduleFragment.transformChildrenVoid(
            SynchronizedFunctionTransformer(pluginContext, lockSymbol, unlockSymbol, jakoSynchronizedSymbol)
        )
    }

    private fun containsSynchronizedFunction(moduleFragment: IrModuleFragment): Boolean {
        var found = false
        moduleFragment.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (!found) {
                    element.acceptChildrenVoid(this)
                }
            }

            override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                if (declaration.hasAnnotation(SYNCHRONIZED_FQ_NAME)) {
                    found = true
                    return
                }
                super.visitSimpleFunction(declaration)
            }
        })
        return found
    }

    companion object {
        private val JAVA_LANG_PACKAGE = FqName("java.lang")
        private val KOTLIN_PACKAGE = FqName("kotlin")
        private val DEPRECATED_FQ_NAME = FqName("kotlin.Deprecated")
        private val SYNCHRONIZED_FQ_NAME = FqName("kotlin.jvm.Synchronized")
        private val LOCK_MONITOR_CALLABLE_ID = CallableId(JAVA_LANG_PACKAGE, Name.identifier("lockObjectMonitor"))
        private val UNLOCK_MONITOR_CALLABLE_ID = CallableId(JAVA_LANG_PACKAGE, Name.identifier("unlockObjectMonitor"))
        private val JAKO_SYNCHRONIZED_CALLABLE_ID = CallableId(KOTLIN_PACKAGE, Name.identifier("jakoSynchronized"))
        private val SYNCHRONIZED_CALLABLE_ID = CallableId(KOTLIN_PACKAGE, Name.identifier("synchronized"))
    }
}

