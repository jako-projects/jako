package jako.test.parity

import java.io.File
import java.io.StringWriter
import java.net.URI
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.fail

/**
 * Automated JDK target verification, whitelist/blacklist documentation audit,
 * and backward-compatibility baseline check executed during `./gradlew check`
 * (`:jako-stdlib:jvmTest`) and `./gradlew checkApiCompatibility`.
 *
 * Target JDK version is configurable via `-Pjako.jdkRelease=<N>` (default: `"8"`),
 * using `javax.tools.JavaCompiler` (`--release <N>`) to inspect the exact symbol table
 * of the target JDK release regardless of the host JVM version.
 */
class JdkSignatureParityTest {

    data class TypeDecl(
        val fqName: String,
        val jdkBinaryName: String,
        val kind: String, // "class", "abstract class", "interface", "enum class", "object"
        val isTopLevel: Boolean,
        val supertypes: Set<String>,
        val constructors: List<Int>,
        val methods: List<MethodDecl>,
        val properties: Set<String>,
        val sourceFile: String
    )

    data class MethodDecl(
        val name: String,
        val paramCount: Int,
        val isVararg: Boolean,
        val isStatic: Boolean
    )

    data class TargetJdkTypeInfo(
        val fqName: String,
        val kind: ElementKind,
        val allSupertypes: Set<String>,
        val methods: List<Pair<String, Pair<Int, Boolean>>>, // (name, (arity, isVarArgs))
        val fields: Set<String>
    )

    companion object {
        val TRACKED_PACKAGES = listOf(
            "java.io", "java.lang", "java.lang.ref", "java.lang.reflect",
            "java.math", "java.net", "java.nio", "java.nio.channels",
            "java.nio.charset", "java.nio.file", "java.nio.file.attribute",
            "java.security", "java.security.cert", "java.security.interfaces",
            "java.security.spec", "java.text", "java.time", "java.time.format",
            "java.time.temporal", "java.util", "java.util.concurrent",
            "java.util.concurrent.atomic", "java.util.concurrent.locks",
            "java.util.function", "java.util.jar", "java.util.logging",
            "java.util.regex", "java.util.stream", "java.util.zip",
            "javax.crypto", "javax.crypto.spec", "javax.net", "javax.net.ssl",
            "javax.security.auth.x500"
        )

        val KOTLIN_MAPPED_TYPES = setOf(
            "java.lang.Object", "java.lang.String", "java.lang.CharSequence", "java.lang.Comparable",
            "java.lang.Throwable", "java.lang.Exception", "java.lang.RuntimeException", "java.lang.Error",
            "java.lang.AutoCloseable", "java.lang.Appendable", "java.lang.Iterable", "java.lang.Cloneable",
            "java.lang.Number", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short", "java.lang.Integer",
            "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Character", "java.lang.Enum",
            "java.lang.NullPointerException", "java.lang.IllegalArgumentException", "java.lang.IllegalStateException",
            "java.lang.UnsupportedOperationException", "java.lang.IndexOutOfBoundsException",
            "java.lang.ArrayIndexOutOfBoundsException", "java.lang.StringIndexOutOfBoundsException",
            "java.lang.ArithmeticException", "java.lang.NumberFormatException", "java.lang.ClassCastException",
            "java.lang.AssertionError", "java.lang.OutOfMemoryError", "java.lang.StackOverflowError",
            "java.lang.NoSuchElementException", "java.lang.SecurityException", "java.lang.CloneNotSupportedException",
            "java.lang.InterruptedException", "java.lang.ReflectiveOperationException",
            "java.lang.Deprecated", "java.lang.Override", "java.lang.SuppressWarnings", "java.lang.SafeVarargs",
            "java.lang.FunctionalInterface", "java.lang.Runnable", "java.lang.Readable",
            "java.util.Collection", "java.util.List", "java.util.Set", "java.util.Map", "java.util.Map.Entry",
            "java.util.Iterator", "java.util.ListIterator", "java.util.RandomAccess", "java.util.Comparator",
            "java.util.AbstractCollection", "java.util.AbstractList", "java.util.AbstractSet", "java.util.AbstractMap",
            "java.util.ArrayList", "java.util.LinkedList", "java.util.HashSet", "java.util.LinkedHashSet",
            "java.util.HashMap", "java.util.LinkedHashMap", "java.util.NoSuchElementException",
            "java.util.ConcurrentModificationException", "java.util.Random"
        )

        private val cachedJdkSnapshots = mutableMapOf<String, Map<String, TargetJdkTypeInfo>>()

        @Synchronized
        fun getTargetJdkSnapshot(release: String): Map<String, TargetJdkTypeInfo> {
            return cachedJdkSnapshots.getOrPut(release) {
                inspectTargetJdk(release)
            }
        }

        private fun inspectTargetJdk(release: String): Map<String, TargetJdkTypeInfo> {
            val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
                ?: error("System JavaCompiler is required to run JdkSignatureParityTest")
            val fm = compiler.getStandardFileManager(null, null, null)
            val dummySource = object : SimpleJavaFileObject(
                URI.create("string:///Dummy.java"),
                JavaFileObject.Kind.SOURCE
            ) {
                override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = "public class Dummy {}"
            }

            val result = LinkedHashMap<String, TargetJdkTypeInfo>()
            val options = listOf("--release", release, "-proc:only")
            val task = compiler.getTask(
                StringWriter(),
                fm,
                null,
                options,
                null,
                listOf(dummySource)
            )
            task.setProcessors(
                listOf(
                    object : AbstractProcessor() {
                        override fun getSupportedAnnotationTypes(): Set<String> = setOf("*")
                        override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
                        override fun process(
                            annotations: Set<TypeElement>,
                            roundEnv: RoundEnvironment
                        ): Boolean {
                            if (roundEnv.processingOver()) return false
                            val elements = processingEnv.elementUtils
                            for (pkgName in TRACKED_PACKAGES) {
                                val pkg = elements.getPackageElement(pkgName) ?: continue
                                for (enclosed in pkg.enclosedElements) {
                                    if (enclosed is TypeElement && Modifier.PUBLIC in enclosed.modifiers) {
                                        visitTypeElement(elements, enclosed, result)
                                    }
                                }
                            }
                            return false
                        }
                    }
                )
            )
            val ok = task.call()
            fm.close()
            check(ok) { "JavaCompiler task with --release $release failed" }
            return result
        }

        private fun visitTypeElement(
            elements: Elements,
            te: TypeElement,
            out: MutableMap<String, TargetJdkTypeInfo>
        ) {
            val fqName = te.qualifiedName.toString()
            val supertypes = LinkedHashSet<String>()
            collectAllJdkSupertypes(te, supertypes)

            val methods = mutableListOf<Pair<String, Pair<Int, Boolean>>>()
            val fields = LinkedHashSet<String>()

            for (member in elements.getAllMembers(te)) {
                val mods = member.modifiers
                if (Modifier.PUBLIC !in mods && Modifier.PROTECTED !in mods) continue
                when (member.kind) {
                    ElementKind.METHOD -> {
                        val ee = member as ExecutableElement
                        methods.add(ee.simpleName.toString() to (ee.parameters.size to ee.isVarArgs))
                    }
                    ElementKind.FIELD, ElementKind.ENUM_CONSTANT -> {
                        fields.add(member.simpleName.toString())
                    }
                    else -> {}
                }
            }
            // Also include protected/public methods from direct superclass (e.g. ZipInputStream.createZipEntry)
            var currSuper = te.superclass
            while (currSuper is DeclaredType) {
                val superEl = currSuper.asElement() as? TypeElement ?: break
                for (member in superEl.enclosedElements) {
                    val mods = member.modifiers
                    if (Modifier.PUBLIC !in mods && Modifier.PROTECTED !in mods) continue
                    if (member.kind == ElementKind.METHOD) {
                        val ee = member as ExecutableElement
                        methods.add(ee.simpleName.toString() to (ee.parameters.size to ee.isVarArgs))
                    } else if (member.kind == ElementKind.FIELD || member.kind == ElementKind.ENUM_CONSTANT) {
                        fields.add(member.simpleName.toString())
                    }
                }
                currSuper = superEl.superclass
            }

            out[fqName] = TargetJdkTypeInfo(
                fqName = fqName,
                kind = te.kind,
                allSupertypes = supertypes,
                methods = methods,
                fields = fields
            )

            for (enc in te.enclosedElements) {
                if (enc is TypeElement) {
                    val mods = enc.modifiers
                    if (Modifier.PUBLIC in mods || Modifier.PROTECTED in mods) {
                        visitTypeElement(elements, enc, out)
                    }
                }
            }
        }

        private fun collectAllJdkSupertypes(te: TypeElement, out: MutableSet<String>) {
            val sc = te.superclass
            if (sc is DeclaredType) {
                val se = sc.asElement() as? TypeElement
                if (se != null && se.qualifiedName.toString() != "java.lang.Object") {
                    if (out.add(se.qualifiedName.toString())) {
                        collectAllJdkSupertypes(se, out)
                    }
                }
            }
            for (iface in te.interfaces) {
                if (iface is DeclaredType) {
                    val ie = iface.asElement() as? TypeElement
                    if (ie != null) {
                        if (out.add(ie.qualifiedName.toString())) {
                            collectAllJdkSupertypes(ie, out)
                        }
                    }
                }
            }
        }
    }

    private fun targetJdkRelease(): String = System.getProperty("jako.jdkRelease", "8")

    private fun findRepoRoot(): File {
        val candidates = listOf(File("."), File(".."), File("../.."))
        return candidates.firstOrNull {
            File(it, "jako-stdlib/src/commonMain/kotlin").isDirectory &&
                File(it, "API_CHECKLIST.md").isFile
        } ?: error("Cannot locate repository root from ${File(".").absolutePath}")
    }

    @Test
    fun testCommonAndNativeSupertypeParity() {
        val repoRoot = findRepoRoot()
        val stdlibDir = File(repoRoot, "jako-stdlib")
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)
        val nativeDecls = mutableMapOf<String, TypeDecl>()
        nativeDecls.putAll(parseSourceTree(File(stdlibDir, "src/nativeMain/kotlin"), isExpect = false))
        nativeDecls.putAll(parseSourceTree(File(stdlibDir, "src/posixMain/kotlin"), isExpect = false))

        val nativeTypealiases = parseActualTypealiases(File(stdlibDir, "src/nativeMain/kotlin"))

        val errors = mutableListOf<String>()

        val cl = Thread.currentThread().contextClassLoader
        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            val expectedSourceFile = if (commonDecl.isTopLevel) {
                resolveReferenceSourceFile(cl, fqName)
            } else {
                null
            }
            if (expectedSourceFile != null && commonDecl.sourceFile != expectedSourceFile) {
                errors.add("File organization mismatch for commonMain $fqName: expected $expectedSourceFile (from stdlib/JDK SourceFile), found ${commonDecl.sourceFile}")
            }
            if (fqName in nativeTypealiases) continue
            val nativeDecl = nativeDecls[fqName]
            if (nativeDecl == null) {
                errors.add("Missing actual declaration in nativeMain/posixMain for expect ${commonDecl.kind} $fqName (${commonDecl.sourceFile})")
                continue
            }
            if (expectedSourceFile != null) {
                val baseName = expectedSourceFile.removeSuffix(".kt")
                if (nativeDecl.sourceFile != "$baseName.native.kt" && nativeDecl.sourceFile != "$baseName.posix.kt") {
                    errors.add("File organization mismatch for nativeMain/posixMain $fqName: expected $baseName.native.kt (or .posix.kt), found ${nativeDecl.sourceFile}")
                }
            }
            if (commonDecl.supertypes != nativeDecl.supertypes) {
                errors.add(
                    "Supertype mismatch for $fqName:\n" +
                        "  commonMain (${commonDecl.sourceFile}): ${commonDecl.supertypes}\n" +
                        "  nativeMain (${nativeDecl.sourceFile}): ${nativeDecl.supertypes}"
                )
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} commonMain vs nativeMain parity error(s):\n" + errors.joinToString("\n"))
        }
    }

    @Test
    fun testCommonSupertypesMatchTargetJdk() {
        val release = targetJdkRelease()
        val jdkSnapshot = getTargetJdkSnapshot(release)
        val stdlibDir = File(findRepoRoot(), "jako-stdlib")
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)

        val trackedBuiltinSupertypes = mapOf(
            "java.lang.AutoCloseable" to "AutoCloseable",
            "java.lang.Comparable" to "Comparable",
            "java.lang.Appendable" to "Appendable",
            "java.lang.Iterable" to "Iterable",
            "java.lang.Cloneable" to "Cloneable",
            "java.lang.Number" to "Number",
            "java.lang.Throwable" to "Throwable",
            "java.lang.Error" to "Error",
            "java.lang.Exception" to "Exception",
            "java.lang.RuntimeException" to "RuntimeException",
            "java.lang.IllegalArgumentException" to "IllegalArgumentException",
            "java.lang.IllegalStateException" to "IllegalStateException",
            "java.lang.UnsupportedOperationException" to "UnsupportedOperationException",
            "java.lang.IndexOutOfBoundsException" to "IndexOutOfBoundsException",
            "java.util.RandomAccess" to "RandomAccess",
            "java.util.Collection" to "MutableCollection",
            "java.util.AbstractCollection" to "AbstractMutableCollection",
            "java.util.List" to "MutableList",
            "java.util.Set" to "MutableSet",
            "java.util.AbstractSet" to "AbstractMutableSet",
            "java.util.Map" to "MutableMap",
            "java.util.AbstractMap" to "AbstractMutableMap",
            "java.util.Iterator" to "Iterator"
        )

        val ignoredJdkSupertypes = setOf(
            // Wrapper/utility classes in jako that only provide static helpers (Kotlin maps primitives to Int/Char):
            "java.lang.Integer->Number",
            "java.lang.Integer->Comparable",
            "java.lang.Integer->Serializable",
            "java.lang.Character->Comparable",
            "java.lang.Character->Serializable",
            // ZonedDateTime in JDK implements ChronoZonedDateTime (Comparable<ChronoZonedDateTime<?>>), not Comparable<ZonedDateTime>
            "java.time.ZonedDateTime->Comparable"
        )

        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("java.") && !fqName.startsWith("javax.")) continue

            val jdkInfo = jdkSnapshot[fqName]
            if (jdkInfo == null) {
                errors.add("Class in commonMain does not exist in target JDK (--release $release): $fqName (${commonDecl.sourceFile})")
                continue
            }

            if (commonDecl.kind == "interface" && jdkInfo.kind != ElementKind.INTERFACE) {
                errors.add("$fqName is declared as interface in commonMain but is ${jdkInfo.kind} in JDK $release")
            }
            if (commonDecl.kind == "enum class" && jdkInfo.kind != ElementKind.ENUM) {
                errors.add("$fqName is declared as enum class in commonMain but is ${jdkInfo.kind} in JDK $release")
            }

            val allCommonSupertypes = collectTransitiveCommonSupertypes(commonDecl, commonDecls)

            for (superFqName in jdkInfo.allSupertypes) {
                val expectedSimpleName = trackedBuiltinSupertypes[superFqName]
                    ?: superFqName.substringAfterLast('.').takeIf {
                        superFqName in commonDecls
                    }
                    ?: continue

                if ("$fqName->$expectedSimpleName" in ignoredJdkSupertypes) continue

                val matched = when (expectedSimpleName) {
                    "MutableCollection" -> "MutableCollection" in allCommonSupertypes || "Collection" in allCommonSupertypes || "AbstractMutableCollection" in allCommonSupertypes || "Queue" in allCommonSupertypes || "MutableSet" in allCommonSupertypes || "MutableList" in allCommonSupertypes
                    "AbstractMutableCollection" -> "AbstractMutableCollection" in allCommonSupertypes || "AbstractMutableSet" in allCommonSupertypes || "MutableCollection" in allCommonSupertypes || "MutableSet" in allCommonSupertypes
                    "MutableList" -> "MutableList" in allCommonSupertypes || "List" in allCommonSupertypes
                    "MutableSet" -> "MutableSet" in allCommonSupertypes || "Set" in allCommonSupertypes
                    "AbstractMutableSet" -> "AbstractMutableSet" in allCommonSupertypes || "MutableSet" in allCommonSupertypes
                    "MutableMap" -> "MutableMap" in allCommonSupertypes || "Map" in allCommonSupertypes || "AbstractMutableMap" in allCommonSupertypes
                    "AbstractMutableMap" -> "AbstractMutableMap" in allCommonSupertypes || "MutableMap" in allCommonSupertypes
                    "Iterator" -> "Iterator" in allCommonSupertypes || "MutableIterator" in allCommonSupertypes
                    else -> expectedSimpleName in allCommonSupertypes
                }

                if (!matched) {
                    errors.add(
                        "$fqName (${commonDecl.sourceFile}) is missing JDK $release supertype '$expectedSimpleName' " +
                            "(from JDK $superFqName; current commonMain supertypes: ${commonDecl.supertypes})"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} commonMain vs JDK $release supertype mismatch(es):\n" + errors.joinToString("\n"))
        }
    }

    @Test
    fun testCommonMembersExistInTargetJdk() {
        val release = targetJdkRelease()
        val jdkSnapshot = getTargetJdkSnapshot(release)
        val stdlibDir = File(findRepoRoot(), "jako-stdlib")
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)

        val kotlinMappedMethodNames = setOf(
            "removeAt", // MutableList.remove(int)
            "toByte", "toShort", "toInt", "toLong", "toFloat", "toDouble", "toChar", // Number.*Value()
            "get", "set", "size", "entries", "keys", "values", "containsKey", "containsValue"
        )
        val kotlinMappedProperties = setOf(
            "size", "entries", "keys", "values", "length", "message", "cause"
        )

        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("java.") && !fqName.startsWith("javax.")) continue
            val jdkInfo = jdkSnapshot[fqName] ?: continue

            val jdkMethodsByName = jdkInfo.methods.groupBy({ it.first }, { it.second })
            val jdkMethodNames = jdkMethodsByName.keys

            for (method in commonDecl.methods) {
                if (method.name in kotlinMappedMethodNames) continue
                val overloads = jdkMethodsByName[method.name]
                if (overloads == null) {
                    errors.add(
                        "Method $fqName.${method.name}/${method.paramCount} declared in ${commonDecl.sourceFile} " +
                            "does not exist in target JDK (--release $release)"
                    )
                    continue
                }
                val arityMatched = overloads.any { (jdkArity, jdkVararg) ->
                    jdkArity == method.paramCount || (jdkVararg && method.paramCount >= jdkArity - 1)
                }
                if (!arityMatched) {
                    errors.add(
                        "Method $fqName.${method.name}/${method.paramCount} declared in ${commonDecl.sourceFile} " +
                            "has incompatible parameter count against target JDK (--release $release) overloads: " +
                            overloads.map { "${it.first}${if (it.second) "*" else ""}" }
                    )
                }
            }

            for (prop in commonDecl.properties.sorted()) {
                if (prop in kotlinMappedProperties || prop in jdkInfo.fields || prop in jdkMethodNames) continue
                val cap = prop.replaceFirstChar { it.uppercaseChar() }
                val hasGetter =
                    "get$cap" in jdkMethodNames ||
                        "is$cap" in jdkMethodNames ||
                        jdkMethodNames.any {
                            it.equals("get$prop", ignoreCase = true) ||
                                it.equals("is$prop", ignoreCase = true) ||
                                (prop.startsWith("is") && it.equals(prop, ignoreCase = true))
                        }
                if (!hasGetter) {
                    errors.add(
                        "Property $fqName.$prop declared in ${commonDecl.sourceFile} " +
                            "does not correspond to any field or getter in target JDK (--release $release)"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} member mismatch(es) against target JDK (--release $release):\n" + errors.joinToString("\n"))
        }
    }

    @Test
    fun testTargetJdkCoverageAndDocumentationSync() {
        val release = targetJdkRelease()
        val jdkSnapshot = getTargetJdkSnapshot(release)
        val repoRoot = findRepoRoot()
        val stdlibDir = File(repoRoot, "jako-stdlib")
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)
        val commonTypealiases = parseCommonTypealiases(File(stdlibDir, "src/commonMain/kotlin"))
        val allImplementedFqNames = commonDecls.keys + commonTypealiases

        val unsupportedDoc = File(repoRoot, "UNSUPPORTED_APIS.md")
        check(unsupportedDoc.isFile) { "Missing UNSUPPORTED_APIS.md at ${unsupportedDoc.absolutePath}" }
        val unsupportedText = unsupportedDoc.readText()
        val section3Text = unsupportedText.substringAfter("## 3.", "").substringBefore("## 4.")
        val blacklistedClasses = Regex("""^[ \t]*-[ \t]+`((?:java|javax)\.[a-z0-9_.]+\.[A-Z][A-Za-z0-9_.]*)`""", RegexOption.MULTILINE)
            .findAll(section3Text)
            .map { it.groupValues[1] }
            .toSet()

        val errors = mutableListOf<String>()

        // 1. Every public class in tracked packages of target JDK must be either implemented, Kotlin-mapped, or blacklisted
        for (fqName in jdkSnapshot.keys.sorted()) {
            if (fqName in allImplementedFqNames || fqName in KOTLIN_MAPPED_TYPES || fqName in blacklistedClasses) {
                continue
            }
            // If nested class inside an unimplemented/blacklisted outer class, outer class accounts for it
            val parts = fqName.split('.')
            val pkg = parts.takeWhile { it.first().isLowerCase() }.joinToString(".")
            val clsParts = parts.dropWhile { it.first().isLowerCase() }
            if (clsParts.size > 1) {
                val outerFq = "$pkg.${clsParts.first()}"
                if (outerFq !in allImplementedFqNames && outerFq !in KOTLIN_MAPPED_TYPES) {
                    continue
                }
            }
            errors.add(
                "Unaccounted JDK $release class '$fqName': neither implemented in commonMain " +
                    "nor listed in UNSUPPORTED_APIS.md Section 3"
            )
        }

        // 2. No implemented class in commonMain may remain on the UNSUPPORTED_APIS.md blacklist
        for (fqName in allImplementedFqNames.sorted()) {
            if (fqName in blacklistedClasses) {
                errors.add(
                    "Class '$fqName' is implemented in commonMain but still listed as unsupported in UNSUPPORTED_APIS.md"
                )
            }
        }

        // 3. Bidirectional sync with API_CHECKLIST.md
        val checklistFile = File(repoRoot, "API_CHECKLIST.md")
        check(checklistFile.isFile) { "Missing API_CHECKLIST.md at ${checklistFile.absolutePath}" }
        val checklistText = checklistFile.readText()

        for ((fqName, decl) in commonDecls.entries.sortedBy { it.key }) {
            if (!decl.isTopLevel) continue
            if (!fqName.startsWith("java.") && !fqName.startsWith("javax.")) continue
            val simpleName = fqName.substringAfterLast('.')
            if (!checklistText.contains("`$simpleName") &&
                !checklistText.contains("`$fqName") &&
                !checklistText.contains(".$simpleName`")
            ) {
                errors.add(
                    "Implemented top-level class '$fqName' (${decl.sourceFile}) is not documented in API_CHECKLIST.md"
                )
            }
        }

        if (errors.isNotEmpty()) {
            fail(
                "Found ${errors.size} JDK $release coverage / documentation sync error(s):\n" +
                    errors.joinToString("\n")
            )
        }
    }

    @Test
    fun testBackwardCompatibilityAgainstBaseline() {
        val repoRoot = findRepoRoot()
        val updateBaseline = System.getProperty("jako.updateApiBaseline")?.toBoolean() == true

        val modules = listOf(
            "jako-stdlib" to File(repoRoot, "jako-stdlib/src/commonMain/kotlin"),
            "jako-android" to File(repoRoot, "jako-android/src/commonMain/kotlin")
        )

        val errors = mutableListOf<String>()

        for ((moduleName, sourceRoot) in modules) {
            if (!sourceRoot.isDirectory) continue
            val currentDump = generateCanonicalApiDump(sourceRoot)
            val baselineFile = File(repoRoot, "api/$moduleName.api")

            if (updateBaseline) {
                baselineFile.parentFile.mkdirs()
                baselineFile.writeText(currentDump)
                continue
            }

            if (!baselineFile.isFile) {
                errors.add(
                    "Missing API baseline file ${baselineFile.relativeTo(repoRoot)}. " +
                        "Run './gradlew dumpApiBaseline' to generate it."
                )
                continue
            }

            val baselineMap = parseApiDumpByClass(baselineFile.readText())
            val currentMap = parseApiDumpByClass(currentDump)

            for ((classHeader, baselineMembers) in baselineMap) {
                val className = classHeader.substringBefore(" :").trim()
                val currentEntry = currentMap.entries.find { it.key.substringBefore(" :").trim() == className }
                if (currentEntry == null) {
                    errors.add("[$moduleName] Incompatible API change: removed public declaration '$className'")
                    continue
                }
                // Check supertypes were not removed
                val baseSupers = extractDumpSupertypes(classHeader)
                val currSupers = extractDumpSupertypes(currentEntry.key)
                val removedSupers = baseSupers - currSupers
                if (removedSupers.isNotEmpty()) {
                    errors.add(
                        "[$moduleName] Incompatible API change in '$className': removed supertype(s) $removedSupers"
                    )
                }
                // Check members (constructors, methods, properties) were not removed
                val removedMembers = baselineMembers - currentEntry.value
                for (rm in removedMembers) {
                    errors.add(
                        "[$moduleName] Incompatible API change in '$className': removed or modified member '$rm'"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail(
                "Found ${errors.size} backward-incompatible API change(s) against committed baseline (api/*.api):\n" +
                    errors.joinToString("\n") +
                    "\nIf this breaking change is intentional, run './gradlew dumpApiBaseline' and review the diff."
            )
        }
    }

    private fun generateCanonicalApiDump(sourceRoot: File): String {
        val decls = parseSourceTree(sourceRoot, isExpect = true).toMutableMap()
        // Also parse concrete classes/interfaces/objects (e.g., in jako-android or commonMain extensions)
        decls.putAll(parseConcreteClasses(sourceRoot))

        val sb = StringBuilder()
        sb.append("# jako Canonical Public API Baseline\n")
        sb.append("# Auto-generated by './gradlew dumpApiBaseline' — DO NOT EDIT MANUALLY\n\n")

        for ((fqName, decl) in decls.entries.sortedBy { it.key }) {
            val superSuffix = if (decl.supertypes.isNotEmpty()) {
                " : " + decl.supertypes.sorted().joinToString(", ")
            } else {
                ""
            }
            sb.append("${decl.kind} $fqName$superSuffix {\n")
            for (ctorArity in decl.constructors.sorted()) {
                sb.append("  constructor/$ctorArity\n")
            }
            for (prop in decl.properties.sorted()) {
                sb.append("  property $prop\n")
            }
            val methodSigs = decl.methods
                .map { "fun ${it.name}/${it.paramCount}${if (it.isVararg) "*" else ""}" }
                .distinct()
                .sorted()
            for (ms in methodSigs) {
                sb.append("  $ms\n")
            }
            sb.append("}\n\n")
        }
        return sb.toString()
    }

    private fun parseApiDumpByClass(dumpText: String): Map<String, Set<String>> {
        val result = LinkedHashMap<String, Set<String>>()
        var currentHeader: String? = null
        var currentMembers = LinkedHashSet<String>()
        for (rawLine in dumpText.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            if (line.endsWith("{")) {
                currentHeader = line.removeSuffix("{").trim()
                currentMembers = LinkedHashSet()
            } else if (line == "}") {
                if (currentHeader != null) {
                    result[currentHeader] = currentMembers
                    currentHeader = null
                }
            } else if (currentHeader != null) {
                currentMembers.add(line)
            }
        }
        return result
    }

    private fun extractDumpSupertypes(header: String): Set<String> {
        if (" :" !in header) return emptySet()
        return header.substringAfter(" :")
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private fun collectTransitiveCommonSupertypes(
        decl: TypeDecl,
        allDecls: Map<String, TypeDecl>
    ): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addAll(decl.supertypes)
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (result.add(curr)) {
                allDecls.values.filter {
                    it.fqName.substringAfterLast('.').substringAfterLast('$') == curr
                }.forEach { match ->
                    queue.addAll(match.supertypes)
                }
                when (curr) {
                    "AbstractQueue" -> { queue.add("AbstractMutableCollection"); queue.add("Queue") }
                    "Queue" -> queue.add("MutableCollection")
                    "Deque" -> queue.add("Queue")
                    "BlockingQueue" -> queue.add("Queue")
                    "BlockingDeque" -> { queue.add("BlockingQueue"); queue.add("Deque") }
                    "NavigableSet" -> queue.add("SortedSet")
                    "SortedSet" -> queue.add("MutableSet")
                    "NavigableMap" -> queue.add("SortedMap")
                    "SortedMap" -> queue.add("MutableMap")
                    "AbstractMutableSet" -> queue.add("MutableSet")
                    "AbstractMutableList" -> { queue.add("AbstractMutableCollection"); queue.add("MutableList") }
                    "MutableSet" -> queue.add("MutableCollection")
                    "MutableList" -> queue.add("MutableCollection")
                    "AbstractMutableCollection" -> queue.add("MutableCollection")
                    "MutableCollection" -> queue.add("Iterable")
                    "AbstractMutableMap" -> queue.add("MutableMap")
                    "MutableIterator" -> queue.add("Iterator")
                    "RuntimeException", "IOException", "GeneralSecurityException" -> queue.add("Exception")
                    "IllegalArgumentException", "IllegalStateException", "UnsupportedOperationException", "IndexOutOfBoundsException", "NoSuchElementException" -> queue.add("RuntimeException")
                    "Exception", "Error" -> queue.add("Throwable")
                    "Throwable" -> queue.add("Serializable")
                    "Enum" -> { queue.add("Comparable"); queue.add("Serializable") }
                }
            }
        }
        if (decl.kind == "enum class") {
            result.add("Comparable")
            result.add("Serializable")
        }
        return result
    }

    private fun parseActualTypealiases(root: File): Set<String> {
        if (!root.isDirectory) return emptySet()
        val result = mutableSetOf<String>()
        root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: ""
            Regex("""^[ \t]*(?:public\s+)?actual\s+typealias\s+(\w+)""", RegexOption.MULTILINE).findAll(content).forEach { m ->
                result.add("$pkg.${m.groupValues[1]}")
            }
        }
        return result
    }

    private fun parseCommonTypealiases(root: File): Set<String> {
        if (!root.isDirectory) return emptySet()
        val result = mutableSetOf<String>()
        root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: ""
            Regex("""^[ \t]*(?:public\s+|expect\s+)*typealias\s+(\w+)""", RegexOption.MULTILINE).findAll(content).forEach { m ->
                result.add("$pkg.${m.groupValues[1]}")
            }
        }
        return result
    }

    private fun parseSourceTree(root: File, isExpect: Boolean): Map<String, TypeDecl> {
        if (!root.isDirectory) return emptyMap()
        val result = mutableMapOf<String, TypeDecl>()
        val kw = if (isExpect) "expect" else "actual"
        val topDeclRegex = Regex(
            """^[ \t]*(?:public\s+|open\s+|abstract\s+|final\s+|sealed\s+)*$kw\s+((?:open\s+|abstract\s+|final\s+|enum\s+|fun\s+)*(?:class|interface|object))\s+(\w+)""",
            RegexOption.MULTILINE
        )
        val nestedDeclRegex = Regex(
            """^[ \t]{4,}(?:public\s+|open\s+|abstract\s+|final\s+|sealed\s+|expect\s+|actual\s+)*((?:open\s+|abstract\s+|final\s+|enum\s+|fun\s+)*(?:class|interface))\s+(\w+)""",
            RegexOption.MULTILINE
        )

        root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: ""

            topDeclRegex.findAll(content).forEach { m ->
                val rawKind = m.groupValues[1].trim().replace(Regex("""\s+"""), " ")
                val name = m.groupValues[2]
                val kind = classifyKind(rawKind)
                val (headerRest, bodyText, _) = extractHeaderAndBody(content, m.range.last + 1)
                val supertypes = extractSupertypes(headerRest)
                val primaryCtors = extractPrimaryConstructorArity(headerRest)
                val fqName = "$pkg.$name"

                val nestedSpans = mutableListOf<IntRange>()
                nestedDeclRegex.findAll(bodyText).forEach { nm ->
                    val nestedRawKind = nm.groupValues[1].trim().replace(Regex("""\s+"""), " ")
                    val nestedName = nm.groupValues[2]
                    val nestedKind = classifyKind(nestedRawKind)
                    val (nestedHeader, nestedBody, nestedEndIndex) = extractHeaderAndBody(bodyText, nm.range.last + 1)
                    val nestedSupertypes = extractSupertypes(nestedHeader)
                    val nestedCtors = extractPrimaryConstructorArity(nestedHeader) + extractSecondaryConstructors(nestedBody)
                    val nestedMethods = extractMethods(nestedBody)
                    val nestedProps = extractProperties(nestedBody)
                    val nestedFqName = "$fqName.$nestedName"
                    val nestedBinaryName = "$fqName\$$nestedName"
                    result[nestedFqName] = TypeDecl(
                        fqName = nestedFqName,
                        jdkBinaryName = nestedBinaryName,
                        kind = nestedKind,
                        isTopLevel = false,
                        supertypes = nestedSupertypes,
                        constructors = nestedCtors,
                        methods = nestedMethods,
                        properties = nestedProps,
                        sourceFile = file.name
                    )
                    nestedSpans.add(nm.range.first until nestedEndIndex)
                }

                var bodyWithoutNestedClasses = bodyText
                for (span in nestedSpans.sortedByDescending { it.first }) {
                    bodyWithoutNestedClasses = bodyWithoutNestedClasses.removeRange(span)
                }

                val methods = extractMethods(bodyWithoutNestedClasses)
                val props = extractProperties(bodyWithoutNestedClasses)
                val ctors = primaryCtors + extractSecondaryConstructors(bodyWithoutNestedClasses)
                result[fqName] = TypeDecl(
                    fqName = fqName,
                    jdkBinaryName = fqName,
                    kind = kind,
                    isTopLevel = true,
                    supertypes = supertypes,
                    constructors = ctors,
                    methods = methods,
                    properties = props,
                    sourceFile = file.name
                )
            }
        }
        return result
    }

    private fun parseConcreteClasses(root: File): Map<String, TypeDecl> {
        if (!root.isDirectory) return emptyMap()
        val result = mutableMapOf<String, TypeDecl>()
        val topDeclRegex = Regex(
            """^(?!private|internal)(?:public\s+|open\s+|abstract\s+|final\s+|sealed\s+)*((?:open\s+|abstract\s+|final\s+|enum\s+|fun\s+)*(?:class|interface|object))\s+(\w+)""",
            RegexOption.MULTILINE
        )
        root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: ""
            topDeclRegex.findAll(content).forEach { m ->
                val fullMatch = m.value
                if ("expect " in fullMatch || "actual " in fullMatch) return@forEach
                val rawKind = m.groupValues[1].trim().replace(Regex("""\s+"""), " ")
                val name = m.groupValues[2]
                val kind = classifyKind(rawKind)
                val (headerRest, bodyText, _) = extractHeaderAndBody(content, m.range.last + 1)
                val supertypes = extractSupertypes(headerRest)
                val ctors = extractPrimaryConstructorArity(headerRest) + extractSecondaryConstructors(bodyText)
                val fqName = "$pkg.$name"
                result[fqName] = TypeDecl(
                    fqName = fqName,
                    jdkBinaryName = fqName,
                    kind = kind,
                    isTopLevel = true,
                    supertypes = supertypes,
                    constructors = ctors,
                    methods = extractMethods(bodyText),
                    properties = extractProperties(bodyText),
                    sourceFile = file.name
                )
            }
        }
        return result
    }

    private fun classifyKind(rawKind: String): String = when {
        "interface" in rawKind -> "interface"
        "enum" in rawKind -> "enum class"
        "abstract" in rawKind -> "abstract class"
        "object" in rawKind -> "object"
        else -> "class"
    }

    private fun extractHeaderAndBody(content: String, startIndex: Int): Triple<String, String, Int> {
        var depth = 0
        var i = startIndex
        while (i < content.length) {
            val ch = content[i]
            if (ch == '(' || ch == '<') depth++
            else if (ch == ')' || ch == '>') depth = maxOf(0, depth - 1)
            else if (ch == '{' && depth == 0) {
                val header = content.substring(startIndex, i)
                var braceDepth = 1
                var j = i + 1
                while (j < content.length && braceDepth > 0) {
                    if (content[j] == '{') braceDepth++
                    else if (content[j] == '}') braceDepth--
                    j++
                }
                val body = content.substring(i + 1, j - 1)
                return Triple(header, body, j)
            } else if (ch == '\n' && depth == 0) {
                val rest = content.substring(i + 1)
                if (Regex("""^[ \t]*(?:expect|actual|public|internal|private|protected|open|abstract|final|sealed|class|interface|object|fun|val|var|typealias|/\*\*|//|@|\}|$)""").containsMatchIn(rest)) {
                    return Triple(content.substring(startIndex, i), "", i)
                }
            }
            i++
        }
        return Triple(content.substring(startIndex), "", content.length)
    }

    private fun extractPrimaryConstructorArity(headerRest: String): List<Int> {
        var s = headerRest.trim()
        while (s.startsWith("<")) {
            var depth = 0
            var cut = -1
            for (i in s.indices) {
                if (s[i] == '<') depth++
                else if (s[i] == '>') {
                    depth--
                    if (depth == 0) { cut = i + 1; break }
                }
            }
            if (cut > 0) s = s.substring(cut).trim() else break
        }
        if (s.startsWith("private") || s.startsWith("internal")) return emptyList()
        s = s.replace(Regex("""^(?:public|protected|actual|expect|\s)+"""), "")
        if (s.startsWith("constructor")) {
            s = s.removePrefix("constructor").trim()
        }
        if (!s.startsWith("(")) return emptyList()
        var depth = 0
        var endIdx = -1
        for (i in s.indices) {
            if (s[i] == '(') depth++
            else if (s[i] == ')') {
                depth--
                if (depth == 0) { endIdx = i; break }
            }
        }
        if (endIdx < 1) return emptyList()
        val paramsStr = s.substring(1, endIdx).trim()
        return listOf(if (paramsStr.isEmpty()) 0 else splitTopLevelParams(paramsStr).size)
    }

    private fun extractSecondaryConstructors(bodyText: String): List<Int> {
        if (bodyText.isBlank()) return emptyList()
        val result = mutableListOf<Int>()
        val ctorRegex = Regex("""^[ \t]*(?!private|internal)(?:public\s+|protected\s+|actual\s+)*constructor\s*\(([^)]*)\)""", RegexOption.MULTILINE)
        ctorRegex.findAll(bodyText).forEach { m ->
            val paramsStr = m.groupValues[1].trim()
            result.add(if (paramsStr.isEmpty()) 0 else splitTopLevelParams(paramsStr).size)
        }
        return result
    }

    private fun extractSupertypes(headerRest: String): Set<String> {
        var s = headerRest.trim()
        while (s.startsWith("<")) {
            var depth = 0
            var cut = -1
            for (i in s.indices) {
                if (s[i] == '<') depth++
                else if (s[i] == '>') {
                    depth--
                    if (depth == 0) { cut = i + 1; break }
                }
            }
            if (cut > 0) s = s.substring(cut).trim() else break
        }
        s = s.replace(Regex("""^(?:public|protected|internal|private|actual|expect|\s)+"""), "")
        if (s.startsWith("constructor")) {
            s = s.removePrefix("constructor").trim()
        }
        if (s.startsWith("(")) {
            var depth = 0
            var cut = -1
            for (i in s.indices) {
                if (s[i] == '(') depth++
                else if (s[i] == ')') {
                    depth--
                    if (depth == 0) { cut = i + 1; break }
                }
            }
            if (cut > 0) s = s.substring(cut).trim()
        }
        if (!s.startsWith(":")) return emptySet()
        s = s.removePrefix(":").trim()

        val sb = StringBuilder()
        var parenDepth = 0
        var angleDepth = 0
        for (ch in s) {
            when (ch) {
                '(' -> parenDepth++
                ')' -> parenDepth = maxOf(0, parenDepth - 1)
                '<' -> angleDepth++
                '>' -> angleDepth = maxOf(0, angleDepth - 1)
                else -> if (parenDepth == 0 && angleDepth == 0) sb.append(ch)
            }
        }
        return sb.toString()
            .split(',')
            .map { it.trim().substringAfterLast('.') }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private fun extractMethods(bodyText: String): List<MethodDecl> {
        if (bodyText.isBlank()) return emptyList()
        val methods = mutableListOf<MethodDecl>()
        val funRegex = Regex("""^[ \t]*(?!private|internal)(?:public\s+|protected\s+|override\s+|open\s+|abstract\s+|operator\s+|actual\s+|expect\s+|final\s+)*fun\s+(?:<[^>]+>\s*)?(\w+)\s*\(([^)]*)\)""", RegexOption.MULTILINE)
        funRegex.findAll(bodyText).forEach { m ->
            val name = m.groupValues[1]
            val paramsStr = m.groupValues[2].trim()
            val paramCount = if (paramsStr.isEmpty()) 0 else splitTopLevelParams(paramsStr).size
            val isVararg = "vararg " in paramsStr
            methods.add(MethodDecl(name, paramCount, isVararg, isStatic = false))
        }
        return methods
    }

    private fun extractProperties(bodyText: String): Set<String> {
        if (bodyText.isBlank()) return emptySet()
        val props = LinkedHashSet<String>()
        val propRegex = Regex("""^[ \t]*(?!private|internal)(?:public\s+|protected\s+|override\s+|open\s+|abstract\s+|actual\s+|expect\s+|final\s+|const\s+)*(?:val|var)\s+(\w+)\s*:""", RegexOption.MULTILINE)
        propRegex.findAll(bodyText).forEach { m ->
            props.add(m.groupValues[1])
        }
        return props
    }

    private fun splitTopLevelParams(params: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var start = 0
        for (i in params.indices) {
            when (params[i]) {
                '<', '(' -> depth++
                '>', ')' -> depth = maxOf(0, depth - 1)
                ',' -> if (depth == 0) {
                    result.add(params.substring(start, i).trim())
                    start = i + 1
                }
            }
        }
        val last = params.substring(start).trim()
        if (last.isNotEmpty()) result.add(last)
        return result
    }

    /**
     * Resolves the expected `.kt` source file name for a top-level class `fqName` by reading the
     * `SourceFile` attribute directly from the reference standard library `.class` bytecode
     * (`kotlin-stdlib` or JDK `jrt:/`), converting `.java` to `.kt`.
     */
    private fun resolveReferenceSourceFile(cl: ClassLoader, fqName: String): String? {
        val resourcePath = fqName.replace('.', '/') + ".class"
        val bytes = cl.getResourceAsStream(resourcePath)?.use { it.readBytes() }
            ?: ClassLoader.getPlatformClassLoader().getResourceAsStream(resourcePath)?.use { it.readBytes() }
            ?: return "${fqName.substringAfterLast('.')}.kt"
        val (sf, _) = parseClassSourceFileAndMembers(bytes)
        return when {
            sf == null -> "${fqName.substringAfterLast('.')}.kt"
            sf.endsWith(".java") -> sf.removeSuffix(".java") + ".kt"
            else -> sf
        }
    }

    /**
     * Verifies that every `kotlin.*` class, annotation, function, and property in `jako-stdlib`
     * resides in the exact `.kt` file recorded in `kotlin-stdlib`'s `.class` `SourceFile` metadata.
     */
    @Test
    fun testKotlinStdlibSourceFileParity() {
        val stdlibJars = System.getProperty("java.class.path", "")
            .split(File.pathSeparator)
            .map { File(it) }
            .filter { it.isFile && it.name.startsWith("kotlin-stdlib") && it.name.endsWith(".jar") }
        if (stdlibJars.isEmpty()) return

        val pkgClassToSourceFile = mutableMapOf<Pair<String, String>, String>()
        val pkgMemberToSourceFiles = mutableMapOf<Pair<String, String>, MutableSet<String>>()

        for (jar in stdlibJars) {
            java.util.jar.JarFile(jar).use { jf ->
                val entries = jf.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    if (!name.endsWith(".class") || "$" in name) continue
                    val bytes = jf.getInputStream(entry).use { it.readBytes() }
                    val (sf, members) = parseClassSourceFileAndMembers(bytes)
                    if (sf == null) continue
                    val pkg = name.substringBeforeLast('/', "").replace('/', '.')
                    val clsSimple = name.substringAfterLast('/').removeSuffix(".class")
                    pkgClassToSourceFile[pkg to clsSimple] = sf
                    for (m in members) {
                        pkgMemberToSourceFiles.getOrPut(pkg to m) { mutableSetOf() }.add(sf)
                    }
                }
            }
        }

        val kotlinRoot = File(findRepoRoot(), "jako-stdlib/src/commonMain/kotlin/kotlin")
        if (!kotlinRoot.isDirectory) return

        val declRegex = Regex(
            """^[ \t]*public\s+(?:inline\s+|infix\s+|operator\s+|expect\s+|actual\s+|open\s+|final\s+|enum\s+|annotation\s+)*(class|interface|object|fun|val|var|typealias)\s+(?:<[^>]+>\s*)?(?:([A-Za-z0-9_.]+)(?:<[^>]+>)?\.)?([A-Za-z0-9_]+)""",
            RegexOption.MULTILINE
        )
        val errors = mutableListOf<String>()

        kotlinRoot.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: "kotlin"
            declRegex.findAll(content).forEach { m ->
                val kind = m.groupValues[1]
                val recv = m.groupValues[2]
                val name = m.groupValues[3]
                if (kind in setOf("class", "interface", "object")) {
                    val expected = pkgClassToSourceFile[pkg to name]
                    if (expected != null && expected != file.name) {
                        errors.add("Kotlin stdlib class '$pkg.$name' is in ${file.name}, but kotlin-stdlib SourceFile is $expected")
                    }
                } else if (kind in setOf("fun", "val", "var")) {
                    val expectedSet = mutableSetOf<String>()
                    pkgMemberToSourceFiles[pkg to name]?.let { expectedSet.addAll(it) }
                    if (kind == "val" || kind == "var") {
                        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
                        pkgMemberToSourceFiles[pkg to getter]?.let { expectedSet.addAll(it) }
                    }
                    if (expectedSet.isNotEmpty() && file.name !in expectedSet) {
                        val qual = if (recv.isNotEmpty()) "$recv.$name" else name
                        errors.add("Kotlin stdlib $kind '$pkg.$qual' is in ${file.name}, but kotlin-stdlib SourceFile is one of $expectedSet")
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} kotlin-stdlib SourceFile organization mismatch(es):\n" + errors.joinToString("\n"))
        }
    }

    private fun parseClassSourceFileAndMembers(data: ByteArray): Pair<String?, Set<String>> {
        val buf = java.nio.ByteBuffer.wrap(data)
        if (buf.remaining() < 10 || buf.int != 0xCAFEBABE.toInt()) return null to emptySet()
        buf.short // minor
        buf.short // major
        val cpCount = buf.short.toInt() and 0xFFFF
        val cpStrings = arrayOfNulls<String>(cpCount)
        var i = 1
        while (i < cpCount) {
            val tag = buf.get().toInt() and 0xFF
            when (tag) {
                1 -> {
                    val len = buf.short.toInt() and 0xFFFF
                    val bytes = ByteArray(len)
                    buf.get(bytes)
                    cpStrings[i] = String(bytes, Charsets.UTF_8)
                }
                3, 4 -> buf.position(buf.position() + 4)
                5, 6 -> {
                    buf.position(buf.position() + 8)
                    i++
                }
                7, 8, 16, 19, 20 -> buf.position(buf.position() + 2)
                9, 10, 11, 12, 17, 18 -> buf.position(buf.position() + 4)
                15 -> buf.position(buf.position() + 3)
            }
            i++
        }
        buf.position(buf.position() + 6) // access, this, super
        val ifCount = buf.short.toInt() and 0xFFFF
        buf.position(buf.position() + 2 * ifCount)

        val members = mutableSetOf<String>()
        val fCount = buf.short.toInt() and 0xFFFF
        repeat(fCount) {
            buf.short // access
            val nameIdx = buf.short.toInt() and 0xFFFF
            buf.short // desc
            cpStrings.getOrNull(nameIdx)?.let { members.add(it) }
            val aCount = buf.short.toInt() and 0xFFFF
            repeat(aCount) {
                buf.short
                val aLen = buf.int
                buf.position(buf.position() + aLen)
            }
        }
        val mCount = buf.short.toInt() and 0xFFFF
        repeat(mCount) {
            buf.short // access
            val nameIdx = buf.short.toInt() and 0xFFFF
            buf.short // desc
            cpStrings.getOrNull(nameIdx)?.let { members.add(it) }
            val aCount = buf.short.toInt() and 0xFFFF
            repeat(aCount) {
                buf.short
                val aLen = buf.int
                buf.position(buf.position() + aLen)
            }
        }
        val aCount = buf.short.toInt() and 0xFFFF
        var sourceFile: String? = null
        repeat(aCount) {
            val aNameIdx = buf.short.toInt() and 0xFFFF
            val aLen = buf.int
            if (cpStrings.getOrNull(aNameIdx) == "SourceFile" && aLen == 2) {
                val sfIdx = buf.short.toInt() and 0xFFFF
                sourceFile = cpStrings.getOrNull(sfIdx)
            } else {
                buf.position(buf.position() + aLen)
            }
        }
        return sourceFile to members
    }
}
