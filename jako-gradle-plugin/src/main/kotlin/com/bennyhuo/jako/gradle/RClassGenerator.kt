package com.bennyhuo.jako.gradle

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.GradleException
import org.w3c.dom.Element
import org.w3c.dom.Node

internal object RClassGenerator {

    private val TYPE_ORDER = listOf(
        "raw" to 0x01,
        "string" to 0x02,
        "integer" to 0x03,
        "bool" to 0x04,
        "color" to 0x05,
        "dimen" to 0x06,
        "array" to 0x07
    )

    internal fun computeEntryHash16(name: String): Int {
        var h = 0x811c9dc5.toInt()
        for (i in 0 until name.length) {
            h = (h xor name[i].code) * 0x01000193
        }
        val folded = ((h ushr 16) xor (h and 0xFFFF)) and 0xFFFF
        return if (folded == 0) 1 else folded
    }

    internal fun sanitizeIdentifier(raw: String): String {
        val replaced = buildString(raw.length) {
            for (ch in raw) {
                if (ch.isLetterOrDigit() || ch == '_') {
                    append(ch)
                } else {
                    append('_')
                }
            }
        }
        if (replaced.isEmpty()) return "_res"
        return if (replaced[0].isDigit()) "_$replaced" else replaced
    }

    fun scanResources(resRoot: File?): Map<String, Map<String, Int>> {
        val byType = LinkedHashMap<String, LinkedHashMap<String, Int>>()
        for ((typeName, _) in TYPE_ORDER) {
            byType[typeName] = LinkedHashMap()
        }

        if (resRoot == null || !resRoot.isDirectory) {
            return byType
        }

        val allocatedIds = HashSet<Int>()
        val keyToId = HashMap<String, Int>()

        fun allocateId(typeCode: Int, typeName: String, rawEntryName: String): Int {
            val normalizedName = rawEntryName.replace('.', '_').replace('-', '_')
            val normKey = "$typeName/$normalizedName"
            val existing = keyToId[normKey]
            if (existing != null) return existing

            val prefix = 0x7f000000 or ((typeCode and 0xFF) shl 16)
            var low16 = computeEntryHash16(normalizedName)
            var id = prefix or low16
            while (!allocatedIds.add(id)) {
                low16 = (low16 % 0xFFFF) + 1
                id = prefix or low16
            }
            keyToId[normKey] = id
            val ident = sanitizeIdentifier(normalizedName)
            byType.getOrPut(typeName) { LinkedHashMap() }[ident] = id
            return id
        }

        // 1. Scan res/raw/* sorted by file name (matching Resources.ensureDiskResourcesLoaded)
        val rawDir = File(resRoot, "raw")
        if (rawDir.isDirectory) {
            val files = rawDir.listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: emptyList()
            for (f in files) {
                val entryName = f.nameWithoutExtension.ifEmpty { f.name }
                allocateId(0x01, "raw", entryName)
            }
        }

        // 2. Scan res/values/*.xml sorted by file name (matching Resources.ensureDiskResourcesLoaded)
        val valuesDir = File(resRoot, "values")
        if (valuesDir.isDirectory) {
            val xmlFiles = valuesDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".xml", ignoreCase = true) }
                ?.sortedBy { it.name }
                ?: emptyList()
            val dbf = DocumentBuilderFactory.newInstance()
            for (xmlFile in xmlFiles) {
                try {
                    val doc = dbf.newDocumentBuilder().parse(xmlFile)
                    val root = doc.documentElement ?: continue
                    val children = root.childNodes
                    for (i in 0 until children.length) {
                        val node = children.item(i)
                        if (node.nodeType != Node.ELEMENT_NODE) continue
                        val elem = node as Element
                        val tag = elem.tagName
                        val name = elem.getAttribute("name")?.trim().orEmpty()
                        if (name.isEmpty()) continue
                        when (tag) {
                            "string" -> allocateId(0x02, "string", name)
                            "integer" -> allocateId(0x03, "integer", name)
                            "bool" -> allocateId(0x04, "bool", name)
                            "color" -> allocateId(0x05, "color", name)
                            "dimen" -> allocateId(0x06, "dimen", name)
                            "string-array", "integer-array", "array" -> allocateId(0x07, "array", name)
                            "item" -> {
                                val itemType = elem.getAttribute("type")?.trim().orEmpty()
                                when (itemType) {
                                    "string" -> allocateId(0x02, "string", name)
                                    "integer" -> allocateId(0x03, "integer", name)
                                    "bool" -> allocateId(0x04, "bool", name)
                                    "color" -> allocateId(0x05, "color", name)
                                    "dimen" -> allocateId(0x06, "dimen", name)
                                    "array" -> allocateId(0x07, "array", name)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    throw GradleException("Failed to parse Android resource XML file: ${xmlFile.absolutePath}", e)
                }
            }
        }

        return byType
    }

    fun generateRFileContent(packageName: String, resRoot: File?): String {
        val resourcesByType = scanResources(resRoot)
        return buildString {
            appendLine("// AUTO-GENERATED FILE BY JAKO GRADLE PLUGIN. DO NOT MODIFY.")
            appendLine("package $packageName")
            appendLine()
            appendLine("public object R {")
            for ((typeName, _) in TYPE_ORDER) {
                val entries = resourcesByType[typeName].orEmpty()
                appendLine("    public object $typeName {")
                for ((ident, id) in entries) {
                    val hex = id.toUInt().toString(16).padStart(8, '0')
                    appendLine("        public const val $ident: Int = 0x$hex")
                }
                appendLine("    }")
            }
            appendLine("}")
        }
    }
}
