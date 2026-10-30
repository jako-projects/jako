package jako.test.net

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatagramSocketTest {

    private inline fun runOrVerifyUnsupportedOnWasi(block: () -> Unit) {
        try {
            block()
        } catch (e: UnsupportedOperationException) {
            val msg = e.message ?: ""
            assertTrue(
                msg.contains("is not supported on target 'WasmWasi'"),
                "Unexpected UnsupportedOperationException: $msg"
            )
        }
    }

    @Test
    fun testDatagramPacketGettersAndSetters() {
        val data = "hello-udp-world".encodeToByteArray()
        val addr = InetAddress.getByName("127.0.0.1")
        val pkt = DatagramPacket(data, 6, 3, addr, 9090)

        assertEquals(6, pkt.offset)
        assertEquals(3, pkt.length)
        assertEquals(addr, pkt.address)
        assertEquals(9090, pkt.port)

        val newData = "replaced".encodeToByteArray()
        pkt.setData(newData, 1, 4)
        assertEquals(1, pkt.offset)
        assertEquals(4, pkt.length)
    }

    @Test
    fun testUdpLoopbackSendAndReceive() = runOrVerifyUnsupportedOnWasi {
        val receiver = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
        val sender = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
        try {
            assertTrue(receiver.isBound)
            assertFalse(receiver.isClosed)
            val port = receiver.localPort
            assertTrue(port > 0)

            val payload = "UDP ping over 127.0.0.1".encodeToByteArray()
            val sendPacket = DatagramPacket(
                payload,
                payload.size,
                InetSocketAddress(InetAddress.getByName("127.0.0.1"), port)
            )
            sender.send(sendPacket)

            val recvBuf = ByteArray(128)
            val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
            receiver.soTimeout = 2000
            receiver.receive(recvPacket)

            assertEquals(payload.size, recvPacket.length)
            assertContentEquals(payload, recvBuf.copyOfRange(0, recvPacket.length))
            assertEquals(sender.localPort, recvPacket.port)

            // Timeout when no packet arrives
            receiver.soTimeout = 40
            assertFailsWith<SocketTimeoutException> {
                receiver.receive(DatagramPacket(ByteArray(16), 16))
            }
        } finally {
            sender.close()
            receiver.close()
            assertTrue(sender.isClosed)
            assertTrue(receiver.isClosed)
        }
    }

    @Test
    fun testRealOsCrossProcessUdpSocket() = runOrVerifyUnsupportedOnWasi {
        if (java.lang.System.getProperty("os.name") == "WASI") {
            DatagramSocket(0)
            return@runOrVerifyUnsupportedOnWasi
        }
        val nodeBin = findNodeExecutable()
        kotlin.test.assertNotNull(nodeBin, "Expected node executable to be available for cross-process UDP verification")

        DatagramSocket(0, InetAddress.getByName("127.0.0.1")).use { receiver ->
            receiver.soTimeout = 5000
            val port = receiver.localPort
            assertTrue(port > 0)

            val expectedPayload = "REAL_OS_UDP_FROM_CHILD_PROCESS_$port"
            val jsScript = "const dgram=require('dgram');" +
                "const s=dgram.createSocket('udp4');" +
                "const msg=Buffer.from('$expectedPayload');" +
                "s.send(msg,0,msg.length,$port,'127.0.0.1',function(err){" +
                "s.close();" +
                "if(err)process.exit(2);" +
                "});"

            val proc = ProcessBuilder(nodeBin, "-e", jsScript).start()
            val exitCode = proc.waitFor()
            val errOut = proc.errorStream.use { it.readBytes().decodeToString() }
            proc.inputStream.close()
            assertEquals(0, exitCode, "Child node UDP sender should exit with 0 (stderr: $errOut)")

            val recvBuf = ByteArray(128)
            val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
            receiver.receive(recvPacket)
            val received = recvBuf.decodeToString(0, recvPacket.length)
            assertEquals(expectedPayload, received)
            assertTrue(recvPacket.port > 0)
        }
    }

    private fun findNodeExecutable(): String? {
        val candidates = mutableListOf<String>()
        val pathEnv = java.lang.System.getenv("PATH") ?: ""
        val sep = java.lang.System.getProperty("path.separator") ?: ":"
        val fileSep = java.lang.System.getProperty("file.separator") ?: "/"
        for (dir in pathEnv.split(sep)) {
            if (dir.isNotEmpty()) {
                candidates.add("$dir${fileSep}node")
                candidates.add("$dir${fileSep}node.exe")
            }
        }
        candidates.addAll(
            listOf(
                "/opt/homebrew/bin/node",
                "/usr/local/bin/node",
                "/usr/bin/node"
            )
        )
        val home = java.lang.System.getProperty("user.home") ?: java.lang.System.getenv("HOME") ?: ""
        if (home.isNotEmpty()) {
            val gradleNodeDir = java.io.File("$home/.gradle/nodejs")
            if (gradleNodeDir.exists() && gradleNodeDir.isDirectory()) {
                gradleNodeDir.listFiles()?.forEach { sub ->
                    val binNode = java.io.File(sub, "bin/node")
                    if (binNode.exists()) candidates.add(binNode.absolutePath)
                    val winNode = java.io.File(sub, "node.exe")
                    if (winNode.exists()) candidates.add(winNode.absolutePath)
                }
            }
        }
        candidates.add("node")
        for (c in candidates) {
            if (c == "node") {
                try {
                    val p = ProcessBuilder("node", "-v").start()
                    val code = p.waitFor()
                    p.inputStream.close()
                    p.errorStream.close()
                    p.outputStream.close()
                    if (code == 0) return "node"
                } catch (_: Throwable) {
                }
            } else {
                val f = java.io.File(c)
                if (f.exists() && f.isFile()) return f.absolutePath
            }
        }
        return null
    }
}
