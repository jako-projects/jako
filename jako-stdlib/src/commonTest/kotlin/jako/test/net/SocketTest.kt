package jako.test.net

import java.net.*

import kotlin.test.*

class SocketTest {

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
    fun testClientServerCommunication() = runOrVerifyUnsupportedOnWasi {
        val serverSocket = ServerSocket(0)
        assertTrue(serverSocket.isBound())
        val localPort = serverSocket.getLocalPort()
        assertTrue(localPort > 0, "Local port should be > 0, got $localPort")

        val clientSocket = Socket()
        assertFalse(clientSocket.isConnected())
        assertFalse(clientSocket.isBound())

        clientSocket.connect(InetSocketAddress("127.0.0.1", localPort))
        assertTrue(clientSocket.isConnected())

        val acceptedSocket = serverSocket.accept()
        assertTrue(acceptedSocket.isConnected())

        // Send data from client to server
        val clientOut = clientSocket.getOutputStream()
        val clientIn = clientSocket.getInputStream()
        val serverOut = acceptedSocket.getOutputStream()
        val serverIn = acceptedSocket.getInputStream()

        clientOut.write("Hello Jako Socket!".encodeToByteArray())
        clientOut.flush()

        val buf = ByteArray(64)
        val readLen = serverIn.read(buf)
        assertTrue(readLen > 0)
        val receivedStr = buf.decodeToString(0, readLen)
        assertEquals("Hello Jako Socket!", receivedStr)

        // Send response back from server to client
        serverOut.write("Hello Back!".encodeToByteArray())
        serverOut.flush()

        val responseBuf = ByteArray(64)
        val respLen = clientIn.read(responseBuf)
        assertTrue(respLen > 0)
        val responseStr = responseBuf.decodeToString(0, respLen)
        assertEquals("Hello Back!", responseStr)

        clientSocket.close()
        acceptedSocket.close()
        serverSocket.close()

        assertTrue(clientSocket.isClosed())
        assertTrue(acceptedSocket.isClosed())
        assertTrue(serverSocket.isClosed())
    }

    @Test
    fun testSocketOptions() {
        val socket = Socket()
        socket.setTcpNoDelay(true)
        assertTrue(socket.getTcpNoDelay())

        socket.setTcpNoDelay(false)
        assertFalse(socket.getTcpNoDelay())

        socket.setSoTimeout(5000)
        assertEquals(5000, socket.getSoTimeout())

        socket.close()
    }

    @Test
    fun testRealOsCrossProcessTcpSocket() = runOrVerifyUnsupportedOnWasi {
        if (java.lang.System.getProperty("os.name") == "WASI") {
            ServerSocket(0)
            return@runOrVerifyUnsupportedOnWasi
        }
        val nodeBin = findNodeExecutable()
        assertNotNull(nodeBin, "Expected node executable to be available for cross-process TCP verification")

        ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")).use { serverSocket ->
            serverSocket.setSoTimeout(5000)
            val port = serverSocket.getLocalPort()
            assertTrue(port > 0)

            val expectedPayload = "REAL_OS_TCP_FROM_CHILD_PROCESS_$port"
            val jsScript = "const net=require('net');" +
                "const s=net.connect($port,'127.0.0.1',function(){" +
                "s.write('$expectedPayload',function(){s.end();s.unref();});" +
                "});" +
                "s.on('error',function(){process.exit(2);});"

            val proc = ProcessBuilder(nodeBin, "-e", jsScript).start()
            val exitCode = proc.waitFor()
            val errOut = proc.errorStream.use { it.readBytes().decodeToString() }
            proc.inputStream.close()
            assertEquals(0, exitCode, "Child node TCP client should exit with 0 (stderr: $errOut)")

            serverSocket.accept().use { accepted ->
                accepted.setSoTimeout(5000)
                val received = accepted.getInputStream().readBytes().decodeToString()
                assertEquals(expectedPayload, received)
            }
        }
    }

    @Test
    fun testHalfClosedSocketAndMultiChunkStreaming() = runOrVerifyUnsupportedOnWasi {
        ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")).use { serverSocket ->
            serverSocket.setSoTimeout(5000)
            val port = serverSocket.getLocalPort()

            Socket("127.0.0.1", port).use { client ->
                client.setSoTimeout(5000)
                serverSocket.accept().use { accepted ->
                    accepted.setSoTimeout(5000)

                    // 1. Write 64KB in multiple chunks from client, then shutdownOutput()
                    val totalSize = 64 * 1024
                    val expected = ByteArray(totalSize) { i -> ((i * 31 + 7) and 0xFF).toByte() }
                    val clientOut = client.getOutputStream()
                    var written = 0
                    val writeStep = 4096
                    while (written < totalSize) {
                        val n = minOf(writeStep, totalSize - written)
                        clientOut.write(expected, written, n)
                        written += n
                    }
                    clientOut.flush()
                    client.shutdownOutput()
                    assertTrue(client.isOutputShutdown())

                    // 2. Server reads in irregular 1000-byte slices until EOF (-1)
                    val serverIn = accepted.getInputStream()
                    val received = ByteArray(totalSize)
                    var totalRead = 0
                    val sliceBuf = ByteArray(1000)
                    while (true) {
                        val r = serverIn.read(sliceBuf, 0, sliceBuf.size)
                        if (r < 0) break
                        sliceBuf.copyInto(received, totalRead, 0, r)
                        totalRead += r
                    }
                    assertEquals(totalSize, totalRead)
                    assertContentEquals(expected, received)

                    // 3. Even though client output is shut down, server can still write response back to client
                    val reply = "ACK:$totalRead".encodeToByteArray()
                    accepted.getOutputStream().write(reply)
                    accepted.getOutputStream().flush()
                    accepted.shutdownOutput()

                    val clientReply = client.getInputStream().readBytes()
                    assertContentEquals(reply, clientReply)
                }
            }
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
