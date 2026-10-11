package jako.test.parity

import java.io.File
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.fail

/**
 * Automated verification running during `./gradlew check` (`:jako-stdlib:jvmTest`)
 * that checks `commonMain` and `nativeMain` declarations against each other and
 * against the real JDK standard library at runtime.
 */
class JdkSignatureParityTest {

    data class TypeDecl(
        val fqName: String,
        val jdkBinaryName: String,
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

    private fun findStdlibDir(): File {
        val candidates = listOf(
            File("."),
            File("jako-stdlib"),
            File("../jako-stdlib")
        )
        return candidates.firstOrNull { File(it, "src/commonMain/kotlin").isDirectory }
            ?: error("Cannot locate jako-stdlib directory from ${File(".").absolutePath}")
    }

    @Test
    fun testCommonAndNativeSupertypeParity() {
        val stdlibDir = findStdlibDir()
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)
        val nativeDecls = mutableMapOf<String, TypeDecl>()
        nativeDecls.putAll(parseSourceTree(File(stdlibDir, "src/nativeMain/kotlin"), isExpect = false))
        nativeDecls.putAll(parseSourceTree(File(stdlibDir, "src/posixMain/kotlin"), isExpect = false))

        val nativeTypealiases = parseActualTypealiases(File(stdlibDir, "src/nativeMain/kotlin"))

        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (fqName in nativeTypealiases) continue
            val nativeDecl = nativeDecls[fqName]
            if (nativeDecl == null) {
                errors.add("Missing actual declaration in nativeMain/posixMain for expect ${commonDecl.kind} $fqName (${commonDecl.sourceFile})")
                continue
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
    fun testCommonSupertypesMatchJdk8() {
        val stdlibDir = findStdlibDir()
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)

        // Standard Kotlin / Java mapped supertypes that exist in commonMain
        val trackedBuiltinSupertypes = mapOf(
            "java.lang.AutoCloseable" to "AutoCloseable",
            "java.lang.Comparable" to "Comparable",
            "java.lang.Appendable" to "Appendable",
            "java.lang.Iterable" to "Iterable",
            "java.lang.Cloneable" to "Cloneable",
            "java.lang.Number" to "Number",
            "java.lang.Throwable" to "Throwable",
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

        // Interfaces added in JDK 9+ (not part of Java 8 baseline) or intentionally omitted in Kotlin
        val ignoredJdkSupertypes = setOf(
            // JDK 19+ / JDK 21+ / JDK 25+ additions:
            "java.util.concurrent.ExecutorService->AutoCloseable",
            "java.util.concurrent.ThreadPoolExecutor->AutoCloseable",
            "java.util.zip.Deflater->AutoCloseable",
            "java.util.zip.Inflater->AutoCloseable",
            // Wrapper/utility classes in jako that only provide static helpers (Kotlin maps primitives to Int/Char):
            "java.lang.Integer->Number",
            "java.lang.Integer->Comparable",
            "java.lang.Integer->Serializable",
            "java.lang.Character->Comparable",
            "java.lang.Character->Serializable",
            // Properties in jako does not inherit legacy Dictionary/Hashtable<Object,Object>
            "java.util.Properties->MutableMap",
            "java.util.Properties->Cloneable",
            // RandomAccessFile / ObjectInputStream / ObjectOutputStream do not implement DataInput/DataOutput in jako
            "java.io.RandomAccessFile->DataInput",
            "java.io.RandomAccessFile->DataOutput",
            "java.io.ObjectInputStream->DataInput",
            "java.io.ObjectOutputStream->DataOutput",
            // ZonedDateTime in JDK implements ChronoZonedDateTime (Comparable<ChronoZonedDateTime<?>>), not Comparable<ZonedDateTime>
            "java.time.ZonedDateTime->Comparable"
        )

        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("java.") && !fqName.startsWith("javax.")) continue

            val jdkClass = try {
                Class.forName(commonDecl.jdkBinaryName)
            } catch (e: ClassNotFoundException) {
                errors.add("Class in commonMain does not exist in JDK: $fqName (${commonDecl.jdkBinaryName})")
                continue
            }

            // 1. Check interface / enum kind
            if (commonDecl.kind == "interface" && !jdkClass.isInterface) {
                errors.add("$fqName is declared as interface in commonMain but is not an interface in JDK")
            }
            if (commonDecl.kind == "enum class" && !jdkClass.isEnum) {
                errors.add("$fqName is declared as enum class in commonMain but is not an enum in JDK")
            }

            // 2. Collect all supertypes of commonDecl (including transitive supertypes within commonDecls)
            val allCommonSupertypes = collectTransitiveCommonSupertypes(commonDecl, commonDecls)

            // 3. Collect direct + transitive JDK supertypes and check against allCommonSupertypes
            val jdkSupertypes = collectAllJdkSupertypes(jdkClass)
            for (jdkSuper in jdkSupertypes) {
                val superFqName = jdkSuper.name
                val normalizedSuperFqName = superFqName.replace('$', '.')
                val expectedSimpleName = trackedBuiltinSupertypes[superFqName]
                    ?: normalizedSuperFqName.substringAfterLast('.').takeIf {
                        normalizedSuperFqName in commonDecls
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
                        "$fqName (${commonDecl.sourceFile}) is missing JDK supertype '$expectedSimpleName' " +
                            "(from JDK ${jdkSuper.name}; current commonMain supertypes: ${commonDecl.supertypes})"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} commonMain vs JDK supertype mismatch(es):\n" + errors.joinToString("\n"))
        }
    }

    @Test
    fun testCommonMethodsExistInJdk() {
        val stdlibDir = findStdlibDir()
        val commonDecls = parseSourceTree(File(stdlibDir, "src/commonMain/kotlin"), isExpect = true)

        // Kotlin collection/builtin method names that map to different bytecode names on JVM
        val kotlinMappedMethodNames = setOf(
            "removeAt", // MutableList.remove(int)
            "toByte", "toShort", "toInt", "toLong", "toFloat", "toDouble", "toChar", // Number.*Value()
            "get", "set", "size", "entries", "keys", "values", "containsKey", "containsValue"
        )

        val errors = mutableListOf<String>()

        for ((fqName, commonDecl) in commonDecls.entries.sortedBy { it.key }) {
            if (!fqName.startsWith("java.") && !fqName.startsWith("javax.")) continue
            val jdkClass = try {
                Class.forName(commonDecl.jdkBinaryName)
            } catch (_: ClassNotFoundException) {
                continue
            }

            val allJdkMethods = (jdkClass.methods.toList() + jdkClass.declaredMethods.toList())
                .filter { Modifier.isPublic(it.modifiers) || Modifier.isProtected(it.modifiers) }
            val jdkMethodNames = allJdkMethods.map { it.name }.toSet()

            for (method in commonDecl.methods) {
                if (method.name in kotlinMappedMethodNames) continue
                if (method.name !in jdkMethodNames) {
                    errors.add(
                        "Method $fqName.${method.name}/${method.paramCount} declared in ${commonDecl.sourceFile} " +
                            "does not exist in JDK ${jdkClass.name}"
                    )
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} method mismatch(es) against JDK:\n" + errors.joinToString("\n"))
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
                    "AbstractQueue" -> { queue.add("AbstractMutableCollection"); queue.add("Queue") }
                    "Queue" -> queue.add("MutableCollection")
                    "Deque" -> queue.add("Queue")
                    "BlockingQueue" -> queue.add("Queue")
                    "BlockingDeque" -> { queue.add("BlockingQueue"); queue.add("Deque") }
                    "NavigableSet" -> queue.add("SortedSet")
                    "SortedSet" -> queue.add("MutableSet")
                    "AbstractMutableSet" -> queue.add("MutableSet")
                    "MutableSet" -> queue.add("MutableCollection")
                    "MutableList" -> queue.add("MutableCollection")
                    "AbstractMutableCollection" -> queue.add("MutableCollection")
                    "MutableCollection" -> queue.add("Iterable")
                    "AbstractMutableMap" -> queue.add("MutableMap")
                    "RuntimeException", "IOException", "GeneralSecurityException" -> queue.add("Exception")
                    "IllegalArgumentException", "IllegalStateException", "UnsupportedOperationException", "IndexOutOfBoundsException", "NoSuchElementException" -> queue.add("RuntimeException")
                    "Exception" -> queue.add("Throwable")
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

    private fun collectAllJdkSupertypes(clazz: Class<*>): Set<Class<*>> {
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

                // Parse nested classes inside bodyText and record their index spans to strip from outer class
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
                        jdkBinaryName = nestedBinaryName,
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
                    jdkBinaryName = fqName,
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
        val funRegex = Regex("""^[ \t]*(?:override\s+|open\s+|abstract\s+|operator\s+|actual\s+|expect\s+)*fun\s+(?:<[^>]+>\s*)?(\w+)\s*\(([^)]*)\)""", RegexOption.MULTILINE)
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
}
