package com.bennyhuo.jako.sample

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object StorageDemo {

    data class StorageReport(
        val appName: String,
        val discoveredFiles: List<String>,
        val compressedSize: Int,
        val decompressedContent: String
    )

    fun run(): StorageReport {
        val tempRoot = Paths.get(System.getProperty("java.io.tmpdir") ?: "/tmp") / "jako-sample-${UUID.randomUUID()}"
        val rootDir = tempRoot.toFile()
        try {
            // 1. NIO Path operations & Kotlin Path extensions (/)
            val configDir = tempRoot / "conf"
            Files.createDirectories(configDir)
            val readmePath = tempRoot / "README.txt"
            readmePath.writeText("Hello from jako KMP storage!")

            // 2. java.util.Properties with FileOutputStream / FileInputStream
            val propFile = (configDir / "app.properties").toFile()
            val writeProps = Properties().apply {
                setProperty("app.name", "jako-sample")
                setProperty("app.env", "multiplatform")
            }
            FileOutputStream(propFile).use { out ->
                writeProps.store(out, "Sample Configuration")
            }

            val loadedProps = Properties()
            FileInputStream(propFile).use { input ->
                loadedProps.load(input)
            }

            // 3. File tree walk & relativeTo using kotlin.io File extensions
            val discovered = rootDir.walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(rootDir).invariantSeparatorsPath }
                .sorted()
                .toList()

            // 4. GZIP compression & decompression in memory
            val payload = readmePath.readText()
            val compressedBytes = ByteArrayOutputStream().use { baos ->
                GZIPOutputStream(baos).use { gzip ->
                    gzip.write(payload.toByteArray())
                }
                baos.toByteArray()
            }

            val decompressed = GZIPInputStream(ByteArrayInputStream(compressedBytes))
                .bufferedReader()
                .use { it.readText() }

            return StorageReport(
                appName = loadedProps.getProperty("app.name") ?: "",
                discoveredFiles = discovered,
                compressedSize = compressedBytes.size,
                decompressedContent = decompressed
            )
        } finally {
            if (tempRoot.exists()) {
                rootDir.deleteRecursively()
            }
        }
    }
}
