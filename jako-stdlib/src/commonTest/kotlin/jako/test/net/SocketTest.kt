package jako.test.net

import java.net.*

import kotlin.test.*

class SocketTest {

    @Test
    fun testClientServerCommunication() {
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
}
