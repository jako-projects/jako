package com.bennyhuo.jako.compiler.synchronized

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

internal class SynchronizedFunctionTransformer(
    private val pluginContext: IrPluginContext,
    private val lockSymbol: IrSimpleFunctionSymbol,
    private val unlockSymbol: IrSimpleFunctionSymbol
) : IrElementTransformerVoid() {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val result = super.visitSimpleFunction(declaration) as IrSimpleFunction
        if (!result.hasAnnotation(SYNCHRONIZED_FQ_NAME)) {
            return result
        }
        result.annotations = result.annotations.filter {
            it.type.classFqName != SYNCHRONIZED_FQ_NAME
        }

        val body = result.body ?: return result

        val builder = DeclarationIrBuilder(
            pluginContext,
            result.symbol,
            result.startOffset,
            result.endOffset
        )

        result.body = builder.irBlockBody {
            val receiverArg = result.dispatchReceiverParameter?.let { irGet(it) } ?: irNull()

            val lockCall = irCall(lockSymbol).apply {
                arguments[0] = receiverArg
            }
            val monitorVar = irTemporary(lockCall, nameHint = "jakoMonitor")

            val tryResultBlock = irBlock {
                when (body) {
                    is IrBlockBody -> {
                        for (stmt in body.statements) {
                            +stmt
                        }
                    }
                    is IrExpressionBody -> {
                        +irReturn(body.expression)
                    }
                    else -> {}
                }
            }

            val unlockCall = irCall(unlockSymbol).apply {
                arguments[0] = irGet(monitorVar)
            }

            +IrTryImpl(
                startOffset = result.startOffset,
                endOffset = result.endOffset,
                type = pluginContext.irBuiltIns.unitType,
                tryResult = tryResultBlock,
                catches = emptyList(),
                finallyExpression = unlockCall
            )
        }
        return result
    }

    companion object {
        private val SYNCHRONIZED_FQ_NAME = FqName("kotlin.jvm.Synchronized")
    }
}
