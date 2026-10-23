package com.bennyhuo.jako.compiler.synchronized

import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.EmptyDeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class JakoSynchronizedFirStatusHook(session: FirSession) : FirStatusTransformerExtension(session) {
    private var patched = false

    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        if (!patched) {
            patched = true
            unDeprecateSynchronizedInSession(session)
        }
        return false
    }

    override fun transformStatus(
        status: FirDeclarationStatus,
        declaration: FirDeclaration
    ): FirDeclarationStatus = status

    private fun unDeprecateSynchronizedInSession(session: FirSession) {
        val symbol = session.symbolProvider.getClassLikeSymbolByClassId(SYNCHRONIZED_CLASS_ID) ?: return
        val firClass = symbol.fir as? FirRegularClass ?: return
        if (firClass.deprecationsProvider !== EmptyDeprecationsProvider || firClass.status.isExpect) {
            firClass.replaceDeprecationsProvider(EmptyDeprecationsProvider)
            firClass.replaceAnnotations(emptyList())
            val oldStatus = firClass.status
            if (oldStatus.isExpect) {
                val newStatus = FirResolvedDeclarationStatusImpl(
                    Visibilities.Public,
                    Modality.FINAL,
                    EffectiveVisibility.Public
                ).apply {
                    isExpect = false
                    isActual = false
                }
                firClass.replaceStatus(newStatus)
            }
        }
    }

    companion object {
        private val SYNCHRONIZED_CLASS_ID = ClassId(FqName("kotlin.jvm"), Name.identifier("Synchronized"))
    }
}
