package jako.test.util.jar

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarException
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.regex.MatchResult
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JarAndRegexAndPosixTest {

    @Test
    fun testMatchResultAndPatternSyntaxException() {
        val pattern = Pattern.compile("(?<user>\\w+)@(\\w+\\.\\w+)(?::(\\d+))?")
        val matcher = pattern.matcher("prefix alice@example.com suffix")
        assertTrue(matcher.find())
        assertEquals(3, matcher.groupCount())
        assertEquals("alice", matcher.group("user"))
        assertEquals("alice", matcher.group(1))
        assertEquals("example.com", matcher.group(2))
        assertNull(matcher.group(3))

        val mr: MatchResult = matcher.toMatchResult()
        // Advance matcher so matcher fails, verifying mr is an immutable snapshot
        matcher.find()
        assertEquals("alice@example.com", mr.group())
        assertEquals("alice", mr.group(1))
        assertEquals("example.com", mr.group(2))
        assertNull(mr.group(3))
        assertEquals(7, mr.start())
        assertEquals(24, mr.end())
        assertEquals(7, mr.start(1))
        assertEquals(12, mr.end(1))
        assertEquals(-1, mr.start(3))
        assertEquals(-1, mr.end(3))

        assertEquals("""\$1\\test""", Matcher.quoteReplacement("$1\\test"))

        val pse = PatternSyntaxException("Unclosed character class", "[abc", 3)
        assertEquals("Unclosed character class", pse.description)
        assertEquals("[abc", pse.pattern)
        assertEquals(3, pse.index)
        assertTrue(pse.message!!.contains("Unclosed character class"))

        assertFailsWith<PatternSyntaxException> {
            Pattern.compile("[unclosed")
        }
    }

    @Test
    fun testManifestAndAttributes() {
        val man = Manifest()
        val main = man.mainAttributes
        main[Attributes.Name.MANIFEST_VERSION] = "1.0"
        main[Attributes.Name.MAIN_CLASS] = "com.example.Main"
        val longClassPath = (1..12).joinToString(" ") { "lib/module-$it.jar" }
        main.putValue("Class-Path", longClassPath)

        val sec = Attributes()
        sec.putValue("Sealed", "true")
        man.entries["com/example/pkg/"] = sec

        val bos = ByteArrayOutputStream()
        man.write(bos)

        val parsed = Manifest(ByteArrayInputStream(bos.toByteArray()))
        assertEquals("1.0", parsed.mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION))
        assertEquals("com.example.Main", parsed.mainAttributes.getValue("main-class"))
        assertEquals(longClassPath, parsed.mainAttributes.getValue(Attributes.Name.CLASS_PATH))
        assertEquals("true", parsed.getAttributes("com/example/pkg/")?.getValue(Attributes.Name.SEALED))
        assertEquals(man, parsed)

        assertFailsWith<IllegalArgumentException> {
            Attributes.Name("")
        }
        assertFailsWith<IllegalArgumentException> {
            Attributes.Name("Invalid Space")
        }
    }

    @Test
    fun testJarFileAndStreams() {
        val tmpFile = File.createTempFile("jako_jar_test_", ".jar")
        try {
            val man = Manifest()
            man.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            man.mainAttributes[Attributes.Name.IMPLEMENTATION_TITLE] = "jako-test"
            val entryAttr = Attributes()
            entryAttr.putValue("Custom-Attr", "hello-attr")
            man.entries["hello.txt"] = entryAttr

            val fos = FileOutputStream(tmpFile)
            val jos = JarOutputStream(fos, man)
            jos.putNextEntry(JarEntry("hello.txt"))
            jos.write("Hello JAR World!".encodeToByteArray())
            jos.closeEntry()
            jos.putNextEntry(JarEntry("dir/sub.txt"))
            jos.write("Sub Content".encodeToByteArray())
            jos.closeEntry()
            jos.close()

            // Read via JarFile
            val jarFile = JarFile(tmpFile)
            try {
                val readMan = jarFile.manifest
                assertNotNull(readMan)
                assertEquals("jako-test", readMan.mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE))

                val je = jarFile.getJarEntry("hello.txt")
                assertNotNull(je)
                assertEquals("hello-attr", je.attributes?.getValue("Custom-Attr"))
                val text = jarFile.getInputStream(je)!!.use { it.readBytes().decodeToString() }
                assertEquals("Hello JAR World!", text)

                val allNames = mutableListOf<String>()
                val en = jarFile.entries()
                while (en.hasMoreElements()) {
                    allNames.add(en.nextElement().name)
                }
                assertTrue(allNames.contains(JarFile.MANIFEST_NAME))
                assertTrue(allNames.contains("hello.txt"))
                assertTrue(allNames.contains("dir/sub.txt"))
            } finally {
                jarFile.close()
            }

            // Read via JarInputStream
            val rawJarBytes = tmpFile.readBytes()
            val jis = JarInputStream(ByteArrayInputStream(rawJarBytes))
            try {
                assertNotNull(jis.manifest)
                assertEquals("jako-test", jis.manifest!!.mainAttributes.getValue("Implementation-Title"))
                val first = jis.nextJarEntry
                assertNotNull(first)
                assertEquals("hello.txt", first.name)
                assertEquals("hello-attr", first.attributes?.getValue("Custom-Attr"))
                val second = jis.nextJarEntry
                assertNotNull(second)
                assertEquals("dir/sub.txt", second.name)
                assertNull(jis.nextJarEntry)
            } finally {
                jis.close()
            }

            val ex = JarException("bad jar")
            assertEquals("bad jar", ex.message)
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun testPosixFilePermissions() {
        val perms = PosixFilePermissions.fromString("rwxr-x---")
        assertTrue(perms.contains(PosixFilePermission.OWNER_READ))
        assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE))
        assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE))
        assertTrue(perms.contains(PosixFilePermission.GROUP_READ))
        assertTrue(perms.contains(PosixFilePermission.GROUP_EXECUTE))
        assertEquals("rwxr-x---", PosixFilePermissions.toString(perms))

        val attr = PosixFilePermissions.asFileAttribute(perms)
        assertEquals("posix:permissions", attr.name())
        assertEquals(perms, attr.value())

        assertFailsWith<IllegalArgumentException> {
            PosixFilePermissions.fromString("rwx")
        }
        assertFailsWith<IllegalArgumentException> {
            PosixFilePermissions.fromString("rwxrwxrwa")
        }

        val osName = java.lang.System.getProperty("os.name")?.lowercase() ?: ""
        if (!osName.contains("win")) {
            val tmpDir = Files.createTempDirectory("jako_posix_test_")
            val file = tmpDir.resolve("script.sh")
            try {
                Files.createFile(file, attr)
                val currentPerms = Files.getPosixFilePermissions(file)
                assertEquals(perms, currentPerms)

                val updatedPerms = PosixFilePermissions.fromString("rw-r--r--")
                Files.setPosixFilePermissions(file, updatedPerms)
                assertEquals(updatedPerms, Files.getPosixFilePermissions(file))
            } finally {
                Files.deleteIfExists(file)
                Files.deleteIfExists(tmpDir)
            }
        }
    }
}
