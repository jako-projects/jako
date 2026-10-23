package com.bennyhuo.jako.compiler

import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class JakoCompilerPluginRegistrarTest {

    @Test
    fun testServiceLoaderLoadsJakoCompilerPluginRegistrar() {
        val registrars = ServiceLoader.load(CompilerPluginRegistrar::class.java).toList()
        val jakoRegistrar = registrars.filterIsInstance<JakoCompilerPluginRegistrar>().singleOrNull()
        assertNotNull(jakoRegistrar, "ServiceLoader should load JakoCompilerPluginRegistrar")
        assertTrue(jakoRegistrar.supportsK2)
        assertEquals("com.bennyhuo.jako.compiler", jakoRegistrar.pluginId)
    }

    @Test
    fun testServiceFileContainsBundledParcelizeMarkerForIdeK2() {
        val serviceResource = JakoCompilerPluginRegistrar::class.java.classLoader
            .getResource("META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar")
        assertNotNull(serviceResource, "CompilerPluginRegistrar service file must exist")

        val content = serviceResource.readText()
        // MainByRegistrarContentBundledFirCompilerPluginProvider in IntelliJ IDEA K2 checks the raw text
        // of META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar for
        // "org.jetbrains.kotlin.parcelize.ParcelizeComponentRegistrar" to activate the IDE-bundled Parcelize plugin.
        assertTrue(
            content.contains("com.bennyhuo.jako.compiler.JakoCompilerPluginRegistrar"),
            "Service file must register JakoCompilerPluginRegistrar"
        )
        assertTrue(
            content.contains("org.jetbrains.kotlin.parcelize.ParcelizeComponentRegistrar"),
            "Service file must contain ParcelizeComponentRegistrar marker for IntelliJ IDEA K2 recognition"
        )
    }
}
