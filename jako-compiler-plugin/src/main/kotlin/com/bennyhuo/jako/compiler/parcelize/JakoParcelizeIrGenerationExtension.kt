package com.bennyhuo.jako.compiler.parcelize

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.parcelize.AndroidSymbols
import org.jetbrains.kotlin.parcelize.ParcelizeIrTransformer
import org.jetbrains.kotlin.parcelize.ParcelizeNames
import org.jetbrains.kotlin.parcelize.isParcelableCreatorIntrinsic
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * Reuses the official Kotlin compiler's [ParcelizeIrTransformer] to generate `@Parcelize` IR,
 * then runs [JakoParcelizeIrPostProcessor] to adapt JVM-specific constructs in the generated IR
 * for Kotlin/Native (`.klib`) compatibility:
 * 1. Relinks synthetic [AndroidSymbols] class/method stubs to real symbols in [IrPluginContext].
 * 2. Rewrites `ClassLoader`-based `readParcelable`/`readBundle` calls to `readTypedObject(CREATOR)`/`readBundle()`.
 * 3. Bridges the static `CREATOR` field into the FIR-synthesized nested `object CREATOR`.
 * 4. Converts `IrExpressionBody` on `describeContents`/`newArray` to `IrBlockBody` required by Kotlin/Native.
 */
class JakoParcelizeIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val parcelClass = pluginContext.referenceClass(JakoParcelizeFirExtension.PARCEL_CLASS_ID) ?: return
        val androidSymbols = AndroidSymbols(pluginContext, moduleFragment)
        ParcelizeIrTransformer(
            pluginContext,
            androidSymbols,
            ParcelizeNames.PARCELIZE_CLASS_FQ_NAMES,
            false
        ).transform(moduleFragment)
        JakoParcelizeIrPostProcessor(pluginContext, androidSymbols, parcelClass).process(moduleFragment)
    }
}

private class JakoParcelizeIrPostProcessor(
    private val pluginContext: IrPluginContext,
    private val androidSymbols: AndroidSymbols,
    private val realParcelClass: IrClassSymbol
) : IrElementTransformerVoid() {

    private val isJvmTarget = pluginContext.platform.isJvm()
    private var currentExpectedType: IrType? = null
    private var currentCollectionType: IrType? = null

    private val realCreatorClass by lazy {
        pluginContext.referenceClass(JakoParcelizeFirExtension.PARCELABLE_CREATOR_CLASS_ID)
    }
    private val writeTypedObjectSymbol by lazy {
        realParcelClass.owner.functions.first { it.name.asString() == "writeTypedObject" && it.regularParams().size == 2 }.symbol
    }
    private val readTypedObjectSymbol by lazy {
        realParcelClass.owner.functions.first { it.name.asString() == "readTypedObject" && it.regularParams().size == 1 }.symbol
    }
    private val readBundleZeroArgSymbol by lazy {
        realParcelClass.owner.functions.first { it.name.asString() == "readBundle" && it.regularParams().isEmpty() }.symbol
    }

    fun process(moduleFragment: IrModuleFragment) {
        moduleFragment.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

            override fun visitClass(declaration: IrClass) {
                declaration.acceptChildrenVoid(this)
                declaration.functions
                    .filter { it.name == ParcelizeNames.DESCRIBE_CONTENTS_NAME }
                    .forEach(::ensureBlockBody)

                val creatorField = declaration.declarations.filterIsInstance<IrField>()
                    .firstOrNull { it.name == ParcelizeNames.CREATOR_NAME && it.isStatic } ?: return
                val creatorObject = declaration.declarations.filterIsInstance<IrClass>()
                    .firstOrNull { it.name == ParcelizeNames.CREATOR_NAME && it.kind == ClassKind.OBJECT } ?: return

                populateCreatorObjectFromField(creatorObject, creatorField)
                if (isJvmTarget) {
                    creatorField.type = remapType(creatorField.type)
                    val builder = DeclarationIrBuilder(pluginContext, creatorField.symbol, creatorField.startOffset, creatorField.endOffset)
                    creatorField.initializer = builder.irExprBody(builder.irGetObject(creatorObject.symbol))
                } else {
                    declaration.declarations.remove(creatorField)
                }
            }
        })
        moduleFragment.transformChildrenVoid(this)
    }

    private fun populateCreatorObjectFromField(creatorObject: IrClass, creatorField: IrField) {
        val ctor = creatorObject.primaryConstructor
        if (ctor != null && ctor.body == null) {
            val builder = DeclarationIrBuilder(pluginContext, ctor.symbol, ctor.startOffset, ctor.endOffset)
            ctor.body = builder.irBlockBody {
                +irDelegatingConstructorCall(pluginContext.irBuiltIns.anyClass.owner.constructors.first())
                +IrInstanceInitializerCallImpl(startOffset, endOffset, creatorObject.symbol, pluginContext.irBuiltIns.unitType)
            }
        }
        val initBlock = creatorField.initializer?.expression as? IrBlock ?: return
        val localCreatorClass = initBlock.statements.filterIsInstance<IrClass>().firstOrNull() ?: return
        moveFunctionBody(localCreatorClass, creatorObject, ParcelizeNames.NEW_ARRAY_NAME)
        moveFunctionBody(localCreatorClass, creatorObject, ParcelizeNames.CREATE_FROM_PARCEL_NAME)
    }

    private fun moveFunctionBody(sourceClass: IrClass, targetClass: IrClass, fnName: Name) {
        val sourceFn = sourceClass.functions.firstOrNull { it.name == fnName } ?: return
        val targetFn = targetClass.functions.firstOrNull { it.name == fnName } ?: return
        val body = sourceFn.body ?: return

        targetFn.isFakeOverride = false
        targetFn.modality = Modality.OPEN
        val paramMap = sourceFn.parameters.zip(targetFn.parameters).toMap()

        targetFn.body = body.transform(object : IrElementTransformerVoid() {
            override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                if (declaration.parent == sourceFn) declaration.parent = targetFn
                return super.visitDeclaration(declaration)
            }

            override fun visitGetValue(expression: IrGetValue): IrExpression {
                val mapped = paramMap[expression.symbol.owner] ?: return super.visitGetValue(expression)
                return IrGetValueImpl(expression.startOffset, expression.endOffset, mapped.type, mapped.symbol, expression.origin)
            }

            override fun visitReturn(expression: IrReturn): IrExpression {
                val transformed = super.visitReturn(expression) as IrReturn
                if (transformed.returnTargetSymbol != sourceFn.symbol) return transformed
                return IrReturnImpl(transformed.startOffset, transformed.endOffset, transformed.type, targetFn.symbol, transformed.value)
            }
        }, null)
        ensureBlockBody(targetFn)
    }

    private fun ensureBlockBody(fn: IrSimpleFunction) {
        val exprBody = fn.body as? IrExpressionBody ?: return
        val builder = DeclarationIrBuilder(pluginContext, fn.symbol, exprBody.startOffset, exprBody.endOffset)
        fn.body = builder.irBlockBody { +irReturn(exprBody.expression) }
    }

    override fun visitExpression(expression: IrExpression): IrExpression {
        expression.type = remapType(expression.type)
        return super.visitExpression(expression)
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        declaration.type = remapType(declaration.type)
        return super.visitVariable(declaration)
    }

    override fun visitValueParameter(declaration: IrValueParameter): IrStatement {
        declaration.type = remapType(declaration.type)
        return super.visitValueParameter(declaration)
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        declaration.returnType = remapType(declaration.returnType)
        return super.visitFunction(declaration)
    }

    override fun visitField(declaration: IrField): IrStatement {
        declaration.type = remapType(declaration.type)
        return super.visitField(declaration)
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
        expression.type = remapType(expression.type)
        expression.typeOperand = remapType(expression.typeOperand)
        val prev = currentExpectedType
        currentExpectedType = expression.typeOperand
        return try {
            super.visitTypeOperator(expression) as IrExpression
        } finally {
            currentExpectedType = prev
        }
    }

    override fun visitVararg(expression: IrVararg): IrExpression {
        val result = super.visitVararg(expression) as IrVararg
        result.type = remapType(result.type)
        result.varargElementType = remapType(result.varargElementType)
        return result
    }

    override fun visitGetField(expression: IrGetField): IrExpression {
        val result = super.visitGetField(expression) as IrGetField
        result.type = remapType(result.type)
        val field = result.symbol.owner
        if (field.name == ParcelizeNames.CREATOR_NAME && result.receiver == null) {
            val parentClass = field.parent as? IrClass
            if (parentClass != null) {
                val realParent = resolveRealClass(parentClass)?.owner ?: parentClass
                buildCreatorAccess(realParent, result.startOffset, result.endOffset)?.let { return it }
            }
        }
        return result
    }

    override fun visitBlock(expression: IrBlock): IrExpression {
        expression.type = remapType(expression.type)
        val prevColl = currentCollectionType
        val prevExpected = currentExpectedType
        currentExpectedType = null
        if ((expression.type as? IrSimpleType)?.arguments?.isNotEmpty() == true) {
            currentCollectionType = expression.type
        }
        return try {
            super.visitBlock(expression)
        } finally {
            currentCollectionType = prevColl
            currentExpectedType = prevExpected
        }
    }

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        val ctor = expression.symbol.owner
        val prevExpected = currentExpectedType
        for (i in 0 until expression.arguments.size) {
            val arg = expression.arguments[i] ?: continue
            val paramType = ctor.parameters.getOrNull(i)?.type
            currentExpectedType = paramType?.takeIf { it.classOrNull != null }?.let(::remapType)
            expression.arguments[i] = arg.transform(this, null)
        }
        currentExpectedType = prevExpected

        expression.type = remapType(expression.type)
        for (i in 0 until expression.typeArguments.size) {
            expression.typeArguments[i] = expression.typeArguments[i]?.let(::remapType)
        }

        val parentClass = ctor.parentAsClass
        if (isSyntheticAndroidClass(parentClass)) {
            val realClass = resolveRealClass(parentClass)?.owner
                ?: error("Cannot resolve real class for synthetic class ${parentClass.name}")
            val realCtor = findMatchingConstructor(realClass, ctor)
            return IrConstructorCallImpl.fromSymbolOwner(
                expression.startOffset, expression.endOffset, expression.type, realCtor.symbol, expression.origin
            ).apply {
                for (i in 0 until typeArguments.size) {
                    typeArguments[i] = expression.typeArguments.getOrNull(i)
                        ?: (expression.type as? IrSimpleType)?.arguments?.getOrNull(i)?.typeOrNull
                        ?: pluginContext.irBuiltIns.anyNType
                }
                for (i in 0 until expression.arguments.size) {
                    arguments[i] = expression.arguments[i]
                }
            }
        }
        return expression
    }

    override fun visitCall(expression: IrCall): IrExpression {
        // Intercept ClassLoader-based calls BEFORE visiting their ClassLoader/KClass.java arguments
        if (expression.symbol == androidSymbols.parcelReadParcelable) {
            val targetType = remapType(
                currentExpectedType ?: expression.typeArguments.getOrNull(0)
                    ?: error("Cannot determine target Parcelable type for readParcelable call")
            )
            currentExpectedType = null
            val parcelArg = expression.arguments[0]?.transform(this, null) ?: error("Missing parcel receiver")
            return buildReadTypedObjectCall(parcelArg, targetType, expression.startOffset, expression.endOffset)
        }

        if (expression.symbol == androidSymbols.parcelReadBundle) {
            val expectedBundleType = remapType(currentExpectedType ?: expression.type)
            currentExpectedType = null
            val parcelArg = expression.arguments[0]?.transform(this, null) ?: error("Missing parcel receiver")
            return buildReadBundleCall(parcelArg, expectedBundleType, expression.startOffset, expression.endOffset)
        }

        val fn = expression.symbol.owner
        val prevExpected = currentExpectedType
        for (i in 0 until expression.arguments.size) {
            val arg = expression.arguments[i] ?: continue
            currentExpectedType = inferCallArgumentExpectedType(fn, expression, i)
                ?.takeIf { it.classOrNull != null }?.let(::remapType)
            expression.arguments[i] = arg.transform(this, null)
        }
        currentExpectedType = prevExpected

        expression.type = remapType(expression.type)
        for (i in 0 until expression.typeArguments.size) {
            expression.typeArguments[i] = expression.typeArguments[i]?.let(::remapType)
        }

        if (fn.isParcelableCreatorIntrinsic()) {
            val targetClass = expression.typeArguments[0]?.classOrNull?.owner
                ?: error("Cannot resolve class for parcelableCreator<T>()")
            return buildCreatorAccess(targetClass, expression.startOffset, expression.endOffset)
                ?: error("Class ${targetClass.name} does not have a CREATOR")
        }

        if (expression.symbol == androidSymbols.parcelWriteParcelable) {
            return IrCallImpl.fromSymbolOwner(
                expression.startOffset, expression.endOffset, pluginContext.irBuiltIns.unitType, writeTypedObjectSymbol
            ).apply {
                typeArguments[0] = expression.arguments[1]!!.type
                arguments[0] = expression.arguments[0]
                arguments[1] = expression.arguments[1]
                arguments[2] = expression.arguments[2]
            }
        }

        val parentClass = fn.parent as? IrClass
        if (parentClass != null && isSyntheticAndroidClass(parentClass)) {
            val realClass = resolveRealClass(parentClass)?.owner
                ?: error("Cannot resolve real class for synthetic class ${parentClass.name}")
            val realFn = findMatchingFunction(realClass, fn)
            return IrCallImpl.fromSymbolOwner(
                expression.startOffset, expression.endOffset, expression.type, realFn.symbol, expression.origin,
                expression.superQualifierSymbol?.let { resolveRealClass(it.owner) ?: it }
            ).apply {
                for (i in 0 until typeArguments.size) typeArguments[i] = expression.typeArguments.getOrNull(i)
                for (i in 0 until expression.arguments.size) arguments[i] = expression.arguments[i]
            }
        }

        return expression
    }

    private fun inferCallArgumentExpectedType(fn: IrSimpleFunction, call: IrCall, argIndex: Int): IrType? {
        val collType = currentCollectionType as? IrSimpleType
        when (fn.name.asString()) {
            "add" -> if (argIndex == 1 && collType != null) return collType.arguments.getOrNull(0)?.typeOrNull
            "set" -> if (argIndex == 2) {
                val arrType = (call.arguments.getOrNull(0)?.type ?: currentCollectionType) as? IrSimpleType
                return arrType?.arguments?.getOrNull(0)?.typeOrNull
            }
            "put" -> if (collType != null && (argIndex == 1 || argIndex == 2)) {
                return collType.arguments.getOrNull(argIndex - 1)?.typeOrNull
            }
        }
        return fn.parameters.getOrNull(argIndex)?.type
    }

    private fun buildReadTypedObjectCall(parcelExpr: IrExpression, targetType: IrType, startOffset: Int, endOffset: Int): IrExpression {
        val nonNullType = targetType.makeNotNull()
        val targetClass = nonNullType.classOrNull?.owner ?: error("Cannot resolve class for $targetType")
        val creatorExpr = buildCreatorAccess(targetClass, startOffset, endOffset)
            ?: error("Parcelable class ${targetClass.name} does not have a CREATOR")
        val readCall = IrCallImpl.fromSymbolOwner(startOffset, endOffset, nonNullType.makeNullable(), readTypedObjectSymbol).apply {
            typeArguments[0] = nonNullType
            arguments[0] = parcelExpr
            arguments[1] = creatorExpr
        }
        return if (targetType.isNullable()) readCall else wrapCheckNotNull(readCall, nonNullType, startOffset, endOffset)
    }

    private fun buildReadBundleCall(parcelExpr: IrExpression, expectedType: IrType, startOffset: Int, endOffset: Int): IrExpression {
        val readCall = IrCallImpl.fromSymbolOwner(startOffset, endOffset, expectedType.makeNullable(), readBundleZeroArgSymbol).apply {
            arguments[0] = parcelExpr
        }
        return if (expectedType.isNullable()) readCall else wrapCheckNotNull(readCall, expectedType.makeNotNull(), startOffset, endOffset)
    }

    private fun wrapCheckNotNull(expr: IrExpression, nonNullType: IrType, startOffset: Int, endOffset: Int): IrExpression =
        IrCallImpl.fromSymbolOwner(startOffset, endOffset, nonNullType, pluginContext.irBuiltIns.checkNotNullSymbol).apply {
            typeArguments[0] = nonNullType
            arguments[0] = expr
        }

    private fun buildCreatorAccess(targetClass: IrClass, startOffset: Int, endOffset: Int): IrExpression? {
        // 1. Nested object CREATOR
        targetClass.declarations.filterIsInstance<IrClass>()
            .firstOrNull { it.name == ParcelizeNames.CREATOR_NAME && it.kind == ClassKind.OBJECT }
            ?.let { return IrGetObjectValueImpl(startOffset, endOffset, it.defaultType, it.symbol) }

        // 2. Companion object property CREATOR or companion implementing Parcelable.Creator
        targetClass.companionObject()?.let { companion ->
            val companionInstance = IrGetObjectValueImpl(startOffset, endOffset, companion.defaultType, companion.symbol)
            companion.properties.firstOrNull { it.name == ParcelizeNames.CREATOR_NAME }?.let { prop ->
                prop.getter?.let { getter ->
                    return IrCallImpl.fromSymbolOwner(startOffset, endOffset, getter.returnType, getter.symbol).apply {
                        arguments[0] = companionInstance
                    }
                }
                prop.backingField?.let { field ->
                    return IrGetFieldImpl(startOffset, endOffset, field.symbol, field.type, if (field.isStatic) null else companionInstance)
                }
            }
            if (realCreatorClass?.owner?.let { companion.isSubclassOf(it) } == true) {
                return companionInstance
            }
        }

        // 3. Direct static property/field CREATOR (JVM)
        targetClass.properties.firstOrNull { it.name == ParcelizeNames.CREATOR_NAME }?.let { prop ->
            prop.backingField?.takeIf { it.isStatic }?.let { field ->
                return IrGetFieldImpl(startOffset, endOffset, field.symbol, remapType(field.type))
            }
            prop.getter?.takeIf { it.parameters.isEmpty() }?.let { getter ->
                return IrCallImpl.fromSymbolOwner(startOffset, endOffset, remapType(getter.returnType), getter.symbol)
            }
        }
        targetClass.declarations.filterIsInstance<IrField>()
            .firstOrNull { it.name == ParcelizeNames.CREATOR_NAME && it.isStatic }
            ?.let { return IrGetFieldImpl(startOffset, endOffset, it.symbol, remapType(it.type)) }

        return null
    }

    private fun remapType(type: IrType): IrType {
        if (type !is IrSimpleType) return type
        val classifier = type.classifier
        val remappedClassifier = if (classifier is IrClassSymbol && isSyntheticAndroidClass(classifier.owner)) {
            resolveRealClass(classifier.owner) ?: classifier
        } else {
            classifier
        }
        val remappedArgs = type.arguments.map { arg ->
            if (arg is IrTypeProjection) {
                val inner = remapType(arg.type)
                if (inner !== arg.type) makeTypeProjection(inner, arg.variance) else arg
            } else arg
        }
        if (remappedClassifier === classifier && remappedArgs == type.arguments) return type
        if (remappedClassifier is IrClassSymbol) {
            val expectedTypeParams = remappedClassifier.owner.typeParameters.size
            val finalArgs = if (remappedArgs.isEmpty() && expectedTypeParams > 0) {
                List(expectedTypeParams) { pluginContext.irBuiltIns.anyNType }
            } else remappedArgs
            val base = remappedClassifier.typeWithArguments(finalArgs)
            return if (type.isNullable()) base.makeNullable() else base
        }
        return type
    }

    private fun getSyntheticOrRealClassId(irClass: IrClass): ClassId? {
        irClass.classId?.let { return it }
        return when (val parent = irClass.parent) {
            is IrPackageFragment -> ClassId(parent.packageFqName, irClass.name)
            is IrClass -> getSyntheticOrRealClassId(parent)?.createNestedClassId(irClass.name)
            else -> null
        }
    }

    private fun resolveRealClass(irClass: IrClass): IrClassSymbol? {
        val classId = getSyntheticOrRealClassId(irClass) ?: return null
        if (!isJvmTarget) {
            CLASS_FALLBACKS[classId.asSingleFqName()]?.let { pluginContext.referenceClass(it)?.let { sym -> return sym } }
        }
        return pluginContext.referenceClass(classId)
            ?: CLASS_FALLBACKS[classId.asSingleFqName()]?.let { pluginContext.referenceClass(it) }
    }

    private fun isSyntheticAndroidClass(irClass: IrClass): Boolean {
        var topParent = irClass.parent
        while (topParent is IrClass) topParent = topParent.parent
        if (topParent is IrFile || topParent !is IrPackageFragment) return false
        val classId = getSyntheticOrRealClassId(irClass) ?: return false
        return pluginContext.referenceClass(classId)?.owner !== irClass
    }

    private fun findMatchingConstructor(realClass: IrClass, syntheticCtor: IrConstructor): IrConstructor {
        val synParams = syntheticCtor.regularParams()
        return realClass.constructors.firstOrNull { candidate ->
            val candParams = candidate.regularParams()
            candParams.size == synParams.size &&
                synParams.indices.all { i -> !synParams[i].type.isInt() || candParams[i].type.isInt() }
        } ?: realClass.constructors.first { it.regularParams().size == synParams.size }
    }

    private fun findMatchingFunction(realClass: IrClass, syntheticFn: IrSimpleFunction): IrSimpleFunction {
        val synSize = syntheticFn.regularParams().size
        return realClass.functions.firstOrNull { it.name == syntheticFn.name && it.regularParams().size == synSize }
            ?: error("Cannot find function ${syntheticFn.name}/$synSize on class ${realClass.name}")
    }

    companion object {
        private val CLASS_FALLBACKS = mapOf(
            FqName("java.util.ArrayList") to ClassId(FqName("kotlin.collections"), Name.identifier("ArrayList")),
            FqName("java.util.LinkedHashMap") to ClassId(FqName("kotlin.collections"), Name.identifier("LinkedHashMap")),
            FqName("java.util.LinkedHashSet") to ClassId(FqName("kotlin.collections"), Name.identifier("LinkedHashSet")),
            FqName("java.util.HashMap") to ClassId(FqName("kotlin.collections"), Name.identifier("HashMap")),
            FqName("java.util.HashSet") to ClassId(FqName("kotlin.collections"), Name.identifier("HashSet")),
            FqName("java.util.List") to ClassId(FqName("kotlin.collections"), Name.identifier("MutableList")),
            FqName("java.util.Map") to ClassId(FqName("kotlin.collections"), Name.identifier("MutableMap")),
            FqName("java.util.Set") to ClassId(FqName("kotlin.collections"), Name.identifier("MutableSet")),
        )
    }
}

private fun IrFunction.regularParams(): List<IrValueParameter> =
    parameters.filter { it.kind == IrParameterKind.Regular }
