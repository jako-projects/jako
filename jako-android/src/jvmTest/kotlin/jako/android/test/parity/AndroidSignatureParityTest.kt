package jako.android.test.parity

import java.io.File
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.fail

/**
 * Automated verification running during `./gradlew check` (`:jako-android:jvmTest`)
 * that checks `commonMain` and `nativeMain` declarations against each other and
 * against the real Android Framework (AOSP via `org.robolectric:android-all`) at runtime.
 */
class AndroidSignatureParityTest {

    data class TypeDecl(
        val fqName: String,
        val binaryName: String,
        val kind: String, // "class", "abstract class", "interface", "enum class", "object"
        val supertypes: Set<String>,
        val methods: List<MethodDecl>,
        val sourceFile: String
    )

    data class MethodDecl(
        val name: String,
        val paramCount: Int,
        val isVararg: Boolean,
        val isStatic: Boolean
    )

    private fun findModuleDir(): File {
        val candidates = listOf(
            File("."),
            File("jako-android"),
            File("../jako-android")
        )
        return candidates.firstOrNull { File(it, "src/commonMain/kotlin").isDirectory }
            ?: File("jako-android").takeIf { it.isDirectory }
            ?: File(".")
    }

    private fun createAndroidAllClassLoader(): ClassLoader {
        val androidAllUrls = System.getProperty("java.class.path", "")
            .split(File.pathSeparator)
            .filter { it.contains("android-all") && it.endsWith(".jar") }
            .map { File(it).toURI().toURL() }
            .toTypedArray()
        return if (androidAllUrls.isNotEmpty()) {
            java.net.URLClassLoader(androidAllUrls, ClassLoader.getPlatformClassLoader())
        } else {
            Thread.currentThread().contextClassLoader
        }
    }

    @Test
    fun testCommonAndNativeSupertypeParity() {
        val moduleDir = findModuleDir()
        val commonDir = File(moduleDir, "src/commonMain/kotlin")
        if (!commonDir.isDirectory) return

        val commonDecls = parseSourceTree(commonDir, isExpect = true)
        val nonJvmDecls = parseSourceTree(File(moduleDir, "src/nonJvmMain/kotlin"), isExpect = false)
        val nativeSharedDecls = parseSourceTree(File(moduleDir, "src/nativeMain/kotlin"), isExpect = false)
        val posixOnlyDecls = parseSourceTree(File(moduleDir, "src/posixMain/kotlin"), isExpect = false)
        val mingwOnlyDecls = parseSourceTree(File(moduleDir, "src/mingwMain/kotlin"), isExpect = false)

        val nativeDecls = mutableMapOf<String, TypeDecl>()
        nativeDecls.putAll(nonJvmDecls)
        nativeDecls.putAll(nativeSharedDecls)
        nativeDecls.putAll(posixOnlyDecls)
        nativeDecls.putAll(parseSourceTree(File(moduleDir, "src/appleMain/kotlin"), isExpect = false))

        val mingwDecls = mutableMapOf<String, TypeDecl>()
        mingwDecls.putAll(nonJvmDecls)
        mingwDecls.putAll(nativeSharedDecls)
        mingwDecls.putAll(mingwOnlyDecls)

        val webDecls = mutableMapOf<String, TypeDecl>()
        webDecls.putAll(nonJvmDecls)
        webDecls.putAll(parseSourceTree(File(moduleDir, "src/webMain/kotlin"), isExpect = false))
        webDecls.putAll(parseSourceTree(File(moduleDir, "src/jsMain/kotlin"), isExpect = false))
        webDecls.putAll(parseSourceTree(File(moduleDir, "src/wasmJsMain/kotlin"), isExpect = false))
        webDecls.putAll(parseSourceTree(File(moduleDir, "src/wasmWasiMain/kotlin"), isExpect = false))

        val nonJvmTypealiases = parseActualTypealiases(File(moduleDir, "src/nonJvmMain/kotlin"))
        val nativeSharedTypealiases = nonJvmTypealiases + parseActualTypealiases(File(moduleDir, "src/nativeMain/kotlin"))
        val nativeTypealiases = nativeSharedTypealiases + parseActualTypealiases(File(moduleDir, "src/posixMain/kotlin"))
        val mingwTypealiases = nativeSharedTypealiases + parseActualTypealiases(File(moduleDir, "src/mingwMain/kotlin"))
        val webTypealiases = nonJvmTypealiases +
            parseActualTypealiases(File(moduleDir, "src/webMain/kotlin")) +
            parseActualTypealiases(File(moduleDir, "src/jsMain/kotlin")) +
            parseActualTypealiases(File(moduleDir, "src/wasmJsMain/kotlin")) +
            parseActualTypealiases(File(moduleDir, "src/wasmWasiMain/kotlin"))

        val errors = mutableListOf<String>()

        val sourceSetSuffixRules = listOf(
            "nonJvmMain" to ".nonJvm.kt",
            "nativeMain" to ".native.kt",
            "posixMain" to ".posix.kt",
            "mingwMain" to ".mingw.kt",
            "webMain" to ".web.kt",
            "jsMain" to ".js.kt",
            "wasmJsMain" to ".wasmJs.kt",
            "wasmWasiMain" to ".wasmWasi.kt"
        )
        for ((sourceSetName, requiredSuffix) in sourceSetSuffixRules) {
            val dir = File(moduleDir, "src/$sourceSetName/kotlin")
            if (!dir.isDirectory) continue
            dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
                if (!file.name.endsWith(requiredSuffix)) {
                    errors.add("File naming convention violation in $sourceSetName: ${file.relativeTo(moduleDir)} must end with '$requiredSuffix'")
                }
            }
        }

        val androidCl = createAndroidAllClassLoader()
        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            val isTopLevel = !commonDecl.binaryName.contains('$')
            val expectedSourceFile = if (isTopLevel) {
                resolveReferenceSourceFile(androidCl, fqName)
            } else {
                null
            }
            if (expectedSourceFile != null && commonDecl.sourceFile != expectedSourceFile) {
                errors.add("File organization mismatch for commonMain $fqName: expected $expectedSourceFile (from Android SDK SourceFile), found ${commonDecl.sourceFile}")
            }
            if (fqName !in nativeTypealiases) {
                val nativeDecl = nativeDecls[fqName]
                if (nativeDecl == null) {
                    errors.add("Missing actual declaration in nonJvmMain/nativeMain/posixMain for expect ${commonDecl.kind} $fqName (${commonDecl.sourceFile})")
                } else {
                    if (expectedSourceFile != null) {
                        val baseName = expectedSourceFile.removeSuffix(".kt")
                        val validNativeFiles = setOf("$baseName.nonJvm.kt", "$baseName.native.kt", "$baseName.posix.kt")
                        if (nativeDecl.sourceFile !in validNativeFiles) {
                            errors.add("File organization mismatch for nonJvmMain/nativeMain/posixMain $fqName: expected one of $validNativeFiles, found ${nativeDecl.sourceFile}")
                        }
                    }
                    if (commonDecl.supertypes != nativeDecl.supertypes) {
                        errors.add(
                            "Supertype mismatch for $fqName (Native POSIX):\n" +
                                "  commonMain (${commonDecl.sourceFile}): ${commonDecl.supertypes}\n" +
                                "  native (${nativeDecl.sourceFile}): ${nativeDecl.supertypes}"
                        )
                    }
                }
            }
            if (fqName !in mingwTypealiases) {
                val mingwDecl = mingwDecls[fqName]
                if (mingwDecl == null) {
                    errors.add("Missing actual declaration in nonJvmMain/nativeMain/mingwMain for expect ${commonDecl.kind} $fqName (${commonDecl.sourceFile})")
                } else {
                    if (expectedSourceFile != null) {
                        val baseName = expectedSourceFile.removeSuffix(".kt")
                        val validMingwFiles = setOf("$baseName.nonJvm.kt", "$baseName.native.kt", "$baseName.mingw.kt")
                        if (mingwDecl.sourceFile !in validMingwFiles) {
                            errors.add("File organization mismatch for nonJvmMain/nativeMain/mingwMain $fqName: expected one of $validMingwFiles, found ${mingwDecl.sourceFile}")
                        }
                    }
                    if (commonDecl.supertypes != mingwDecl.supertypes) {
                        errors.add(
                            "Supertype mismatch for $fqName (Native Mingw):\n" +
                                "  commonMain (${commonDecl.sourceFile}): ${commonDecl.supertypes}\n" +
                                "  mingw (${mingwDecl.sourceFile}): ${mingwDecl.supertypes}"
                        )
                    }
                }
            }
            if (fqName !in webTypealiases) {
                val webDecl = webDecls[fqName]
                if (webDecl == null) {
                    errors.add("Missing actual declaration in nonJvmMain/webMain/jsMain/wasmJsMain for expect ${commonDecl.kind} $fqName (${commonDecl.sourceFile})")
                } else {
                    if (expectedSourceFile != null) {
                        val baseName = expectedSourceFile.removeSuffix(".kt")
                        val validWebFiles = setOf("$baseName.nonJvm.kt", "$baseName.web.kt", "$baseName.js.kt", "$baseName.wasmJs.kt", "$baseName.wasmWasi.kt")
                        if (webDecl.sourceFile !in validWebFiles) {
                            errors.add("File organization mismatch for nonJvmMain/webMain $fqName: expected one of $validWebFiles, found ${webDecl.sourceFile}")
                        }
                    }
                    if (commonDecl.supertypes != webDecl.supertypes) {
                        errors.add(
                            "Supertype mismatch for $fqName (Web):\n" +
                                "  commonMain (${commonDecl.sourceFile}): ${commonDecl.supertypes}\n" +
                                "  web (${webDecl.sourceFile}): ${webDecl.supertypes}"
                        )
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} commonMain vs nonJvmMain/nativeMain/posixMain/mingwMain/webMain parity error(s):\n" + errors.joinToString("\n"))
        }

        val jvmDir = File(moduleDir, "src/jvmMain/kotlin")
        if (jvmDir.isDirectory) {
            val jvmTopClassRegex = Regex(
                """^(?!internal\s+|private\s+)(?:public\s+|open\s+|abstract\s+|final\s+|sealed\s+)*(?:class|interface|object)\s+(\w+)""",
                RegexOption.MULTILINE
            )
            jvmDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                val content = file.readText()
                val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(content)?.groupValues?.get(1) ?: ""
                jvmTopClassRegex.findAll(content).forEach { m ->
                    val name = m.groupValues[1]
                    val fqName = "$pkg.$name"
                    val expectedJvmFile = resolveReferenceSourceFile(androidCl, fqName) ?: "$name.kt"
                    if (file.name != expectedJvmFile) {
                        errors.add("File organization mismatch for jvmMain $fqName: expected $expectedJvmFile, found ${file.name}")
                    }
                }
            }
            if (errors.isNotEmpty()) {
                fail("Found ${errors.size} jvmMain file organization error(s):\n" + errors.joinToString("\n"))
            }
        }
    }

    @Test
    fun testCommonSupertypesMatchAndroidFramework() {
        val moduleDir = findModuleDir()
        val commonDir = File(moduleDir, "src/commonMain/kotlin")
        if (!commonDir.isDirectory) return

        val commonDecls = parseSourceTree(commonDir, isExpect = true)

        val trackedBuiltinSupertypes = mapOf(
            "java.lang.AutoCloseable" to "AutoCloseable",
            "java.io.Closeable" to "Closeable",
            "java.io.Serializable" to "Serializable",
            "java.lang.Comparable" to "Comparable",
            "java.lang.Appendable" to "Appendable",
            "java.lang.CharSequence" to "CharSequence",
            "java.lang.Iterable" to "Iterable",
            "java.lang.Cloneable" to "Cloneable",
            "java.lang.Number" to "Number",
            "java.lang.Throwable" to "Throwable",
            "java.lang.Exception" to "Exception",
            "java.lang.RuntimeException" to "RuntimeException",
            "java.util.Collection" to "MutableCollection",
            "java.util.List" to "MutableList",
            "java.util.Set" to "MutableSet",
            "java.util.Map" to "MutableMap",
            "android.os.Parcelable" to "Parcelable"
        )

        val ignoredAndroidSupertypes = setOf<String>(
            // Add intentional omissions if any (e.g. Parcelable before Milestone 4 is implemented)
        )

        val androidClassLoader = createAndroidAllClassLoader()
        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("android.") && !fqName.startsWith("org.json.") && !fqName.startsWith("org.xmlpull.v1.")) continue

            val androidClass = try {
                Class.forName(commonDecl.binaryName, false, androidClassLoader)
            } catch (e: ClassNotFoundException) {
                errors.add("Class in commonMain does not exist in Android Framework: $fqName (${commonDecl.binaryName})")
                continue
            }

            if (commonDecl.kind == "interface" && !androidClass.isInterface) {
                errors.add("$fqName is declared as interface in commonMain but is not an interface in Android")
            }
            if (commonDecl.kind == "enum class" && !androidClass.isEnum) {
                errors.add("$fqName is declared as enum class in commonMain but is not an enum in Android")
            }

            val allCommonSupertypes = collectTransitiveCommonSupertypes(commonDecl, commonDecls)
            val androidSupertypes = collectAllSupertypes(androidClass)

            for (androidSuper in androidSupertypes) {
                val superFqName = androidSuper.name
                val normalizedSuperFqName = superFqName.replace('$', '.')
                val expectedSimpleName = trackedBuiltinSupertypes[superFqName]
                    ?.takeIf {
                        // Only enforce Parcelable once android.os.Parcelable is declared in commonDecls
                        it != "Parcelable" || "android.os.Parcelable" in commonDecls
                    }
                    ?: normalizedSuperFqName.substringAfterLast('.').takeIf {
                        normalizedSuperFqName in commonDecls
                    }
                    ?: continue

                if ("$fqName->$expectedSimpleName" in ignoredAndroidSupertypes) continue

                val matched = when (expectedSimpleName) {
                    "MutableCollection" -> "MutableCollection" in allCommonSupertypes || "Collection" in allCommonSupertypes || "MutableSet" in allCommonSupertypes || "MutableList" in allCommonSupertypes
                    "MutableList" -> "MutableList" in allCommonSupertypes || "List" in allCommonSupertypes
                    "MutableSet" -> "MutableSet" in allCommonSupertypes || "Set" in allCommonSupertypes
                    "MutableMap" -> "MutableMap" in allCommonSupertypes || "Map" in allCommonSupertypes
                    "AutoCloseable" -> "AutoCloseable" in allCommonSupertypes || "Closeable" in allCommonSupertypes
                    else -> expectedSimpleName in allCommonSupertypes
                }

                if (!matched) {
                    errors.add(
                        "$fqName (${commonDecl.sourceFile}) is missing Android supertype '$expectedSimpleName' " +
                            "(from Android ${androidSuper.name}; current commonMain supertypes: ${commonDecl.supertypes})"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} commonMain vs Android Framework supertype mismatch(es):\n" + errors.joinToString("\n"))
        }
    }

    @Test
    fun testCommonMethodsExistInAndroidFramework() {
        val moduleDir = findModuleDir()
        val commonDir = File(moduleDir, "src/commonMain/kotlin")
        if (!commonDir.isDirectory) return

        val commonDecls = parseSourceTree(commonDir, isExpect = true)

        val kotlinMappedMethodNames = setOf(
            "removeAt",
            "toByte", "toShort", "toInt", "toLong", "toFloat", "toDouble", "toChar",
            "get", "set", "size", "entries", "keys", "values", "containsKey", "containsValue"
        )

        val androidClassLoader = createAndroidAllClassLoader()
        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("android.") && !fqName.startsWith("org.json.") && !fqName.startsWith("org.xmlpull.v1.")) continue
            val androidClass = try {
                Class.forName(commonDecl.binaryName, false, androidClassLoader)
            } catch (_: ClassNotFoundException) {
                continue
            }

            val allAndroidMethods = (androidClass.methods.toList() + androidClass.declaredMethods.toList())
                .filter { Modifier.isPublic(it.modifiers) || Modifier.isProtected(it.modifiers) }
            val androidMethodNames = allAndroidMethods.map { it.name }.toSet()

            for (method in commonDecl.methods) {
                if (method.name in kotlinMappedMethodNames) continue
                if (method.name !in androidMethodNames) {
                    errors.add(
                        "Method $fqName.${method.name}/${method.paramCount} declared in ${commonDecl.sourceFile} " +
                            "does not exist in Android Framework ${androidClass.name}"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} method mismatch(es) against Android Framework:\n" + errors.joinToString("\n"))
        }
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
                val match = allDecls.values.firstOrNull {
                    it.fqName.substringAfterLast('.').substringAfterLast('$') == curr
                }
                if (match != null) {
                    queue.addAll(match.supertypes)
                }
                when (curr) {
                    "MutableSet" -> queue.add("MutableCollection")
                    "MutableList" -> queue.add("MutableCollection")
                    "MutableCollection" -> queue.add("Iterable")
                    "Closeable" -> queue.add("AutoCloseable")
                    "Number" -> queue.add("Serializable")
                    "RuntimeException", "IOException" -> queue.add("Exception")
                    "Exception" -> queue.add("Throwable")
                    "Throwable" -> queue.add("Serializable")
                }
            }
        }
        if (decl.kind == "enum class") {
            result.add("Comparable")
            result.add("Serializable")
        }
        return result
    }

    private fun collectAllSupertypes(clazz: Class<*>): Set<Class<*>> {
        val result = LinkedHashSet<Class<*>>()
        fun visit(c: Class<*>?) {
            if (c == null || c == Any::class.java || !result.add(c)) return
            visit(c.superclass)
            for (iface in c.interfaces) {
                visit(iface)
            }
        }
        visit(clazz.superclass)
        for (iface in clazz.interfaces) {
            visit(iface)
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
                val fqName = "$pkg.$name"

                val nestedSpans = mutableListOf<IntRange>()
                nestedDeclRegex.findAll(bodyText).forEach { nm ->
                    val nestedRawKind = nm.groupValues[1].trim().replace(Regex("""\s+"""), " ")
                    val nestedName = nm.groupValues[2]
                    val nestedKind = classifyKind(nestedRawKind)
                    val (nestedHeader, nestedBody, nestedEndIndex) = extractHeaderAndBody(bodyText, nm.range.last + 1)
                    val nestedSupertypes = extractSupertypes(nestedHeader)
                    val nestedMethods = extractMethods(nestedBody)
                    val nestedFqName = "$fqName.$nestedName"
                    val nestedBinaryName = "$fqName\$$nestedName"
                    result[nestedFqName] = TypeDecl(
                        fqName = nestedFqName,
                        binaryName = nestedBinaryName,
                        kind = nestedKind,
                        supertypes = nestedSupertypes,
                        methods = nestedMethods,
                        sourceFile = file.name
                    )
                    nestedSpans.add(nm.range.first until nestedEndIndex)
                }

                var bodyWithoutNestedClasses = bodyText
                for (span in nestedSpans.sortedByDescending { it.first }) {
                    bodyWithoutNestedClasses = bodyWithoutNestedClasses.removeRange(span)
                }

                val methods = extractMethods(bodyWithoutNestedClasses)
                result[fqName] = TypeDecl(
                    fqName = fqName,
                    binaryName = fqName,
                    kind = kind,
                    supertypes = supertypes,
                    methods = methods,
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
        val funRegex = Regex("""^[ \t]*(?:override\s+|open\s+|abstract\s+|operator\s+|actual\s+|expect\s+)*fun\s+(?:<[^>]+>\s*)?`?(\w+)`?\s*\(([^)]*)\)""", RegexOption.MULTILINE)
        funRegex.findAll(bodyText).forEach { m ->
            val name = m.groupValues[1]
            val paramsStr = m.groupValues[2].trim()
            val paramCount = if (paramsStr.isEmpty()) 0 else splitTopLevelParams(paramsStr).size
            val isVararg = "vararg " in paramsStr
            methods.add(MethodDecl(name, paramCount, isVararg, isStatic = false))
        }
        return methods
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

    private fun resolveReferenceSourceFile(cl: ClassLoader, fqName: String): String? {
        val resourcePath = fqName.replace('.', '/') + ".class"
        val bytes = cl.getResourceAsStream(resourcePath)?.use { it.readBytes() }
            ?: return "${fqName.substringAfterLast('.')}.kt"
        val sf = extractClassSourceFile(bytes)
        return when {
            sf == null -> "${fqName.substringAfterLast('.')}.kt"
            sf.endsWith(".java") -> sf.removeSuffix(".java") + ".kt"
            else -> sf
        }
    }

    private fun extractClassSourceFile(data: ByteArray): String? {
        val buf = java.nio.ByteBuffer.wrap(data)
        if (buf.remaining() < 10 || buf.int != 0xCAFEBABE.toInt()) return null
        buf.short
        buf.short
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
        buf.position(buf.position() + 6)
        val ifCount = buf.short.toInt() and 0xFFFF
        buf.position(buf.position() + 2 * ifCount)
        val fCount = buf.short.toInt() and 0xFFFF
        repeat(fCount) {
            buf.position(buf.position() + 6)
            val aCount = buf.short.toInt() and 0xFFFF
            repeat(aCount) {
                buf.short
                val aLen = buf.int
                buf.position(buf.position() + aLen)
            }
        }
        val mCount = buf.short.toInt() and 0xFFFF
        repeat(mCount) {
            buf.position(buf.position() + 6)
            val aCount = buf.short.toInt() and 0xFFFF
            repeat(aCount) {
                buf.short
                val aLen = buf.int
                buf.position(buf.position() + aLen)
            }
        }
        val aCount = buf.short.toInt() and 0xFFFF
        repeat(aCount) {
            val aNameIdx = buf.short.toInt() and 0xFFFF
            val aLen = buf.int
            if (cpStrings.getOrNull(aNameIdx) == "SourceFile" && aLen == 2) {
                val sfIdx = buf.short.toInt() and 0xFFFF
                return cpStrings.getOrNull(sfIdx)
            } else {
                buf.position(buf.position() + aLen)
            }
        }
        return null
    }
}
