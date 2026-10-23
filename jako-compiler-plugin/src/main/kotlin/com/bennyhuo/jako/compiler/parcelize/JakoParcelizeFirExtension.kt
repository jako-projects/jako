package com.bennyhuo.jako.compiler.parcelize

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.parcelize.ParcelizeNames

/**
 * Complements official [org.jetbrains.kotlin.parcelize.fir.FirParcelizeDeclarationGenerator]
 * by synthesizing nested `object CREATOR : Parcelable.Creator<T>` on non-sealed `@Parcelize` classes
 * so `CREATOR` is exported into KMP metadata (`.klib`) and directly resolvable in `commonMain`.
 */
class JakoParcelizeFirExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {

    object Key : GeneratedDeclarationKey()

    private val parcelizePredicate = LookupPredicate.create {
        annotated(ParcelizeNames.PARCELIZE_CLASS_FQ_NAMES)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(parcelizePredicate)
    }

    private fun isParcelizeClass(symbol: FirClassSymbol<*>): Boolean =
        session.predicateBasedProvider.matches(parcelizePredicate, symbol) &&
            symbol.rawStatus.modality != Modality.SEALED

    private fun isGeneratedCreator(symbol: FirClassSymbol<*>): Boolean =
        (symbol.origin as? FirDeclarationOrigin.Plugin)?.key == Key &&
            symbol.classId.shortClassName == ParcelizeNames.CREATOR_NAME

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> = if (isParcelizeClass(classSymbol)) setOf(ParcelizeNames.CREATOR_NAME) else emptySet()

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if (name != ParcelizeNames.CREATOR_NAME || !isParcelizeClass(owner)) return null
        val ownerClass = owner as? FirRegularClassSymbol ?: return null
        val typeArgs = ownerClass.typeParameterSymbols.map { it.toConeType() }.toTypedArray()
        val ownerType = ownerClass.classId.constructClassLikeType(typeArgs, isMarkedNullable = false)
        return createNestedClass(owner, ParcelizeNames.CREATOR_NAME, Key, ClassKind.OBJECT) {
            visibility = Visibilities.Public
            modality = Modality.FINAL
            superType(PARCELABLE_CREATOR_CLASS_ID.constructClassLikeType(arrayOf(ownerType), isMarkedNullable = false))
        }.symbol
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> = if (isGeneratedCreator(classSymbol)) {
        setOf(SpecialNames.INIT, ParcelizeNames.CREATE_FROM_PARCEL_NAME, ParcelizeNames.NEW_ARRAY_NAME)
    } else {
        emptySet()
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        if (!isGeneratedCreator(context.owner)) return emptyList()
        return listOf(createConstructor(context.owner, Key, isPrimary = true) { visibility = Visibilities.Private }.symbol)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner?.takeIf(::isGeneratedCreator) ?: return emptyList()
        val outerClassId = owner.classId.outerClassId ?: return emptyList()
        val outerType = outerClassId.constructClassLikeType(emptyArray(), isMarkedNullable = false)
        return when (callableId.callableName) {
            ParcelizeNames.CREATE_FROM_PARCEL_NAME -> listOf(
                createMemberFunction(owner, Key, callableId.callableName, outerType) {
                    visibility = Visibilities.Public
                    modality = Modality.OPEN
                    valueParameter(Name.identifier("source"), PARCEL_CLASS_ID.constructClassLikeType(emptyArray(), false))
                }.symbol
            )
            ParcelizeNames.NEW_ARRAY_NAME -> {
                val nullableOuter = outerClassId.constructClassLikeType(emptyArray(), isMarkedNullable = true)
                val arrayType = StandardClassIds.Array.constructClassLikeType(arrayOf(nullableOuter), false)
                listOf(
                    createMemberFunction(owner, Key, callableId.callableName, arrayType) {
                        visibility = Visibilities.Public
                        modality = Modality.OPEN
                        valueParameter(Name.identifier("size"), session.builtinTypes.intType.coneType)
                    }.symbol
                )
            }
            else -> emptyList()
        }
    }

    companion object {
        val PARCEL_CLASS_ID = ClassId(FqName("android.os"), Name.identifier("Parcel"))
        val PARCELABLE_CLASS_ID = ClassId(FqName("android.os"), Name.identifier("Parcelable"))
        val PARCELABLE_CREATOR_CLASS_ID = PARCELABLE_CLASS_ID.createNestedClassId(Name.identifier("Creator"))
    }
}
