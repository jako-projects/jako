package jako.test.net

import java.net.*

import javax.net.*

import kotlin.test.*

class SocketFactoryTest {

    @Test
    fun testDefaultSocketFactory() {
        val factory = SocketFactory.getDefault()
        assertNotNull(factory)

        val socket = factory.createSocket()
        assertNotNull(socket)
        assertFalse(socket.isConnected())
        socket.close()
    }

    @Test
    fun testDefaultServerSocketFactory() {
        val serverFactory = ServerSocketFactory.getDefault()
        assertNotNull(serverFactory)

        val serverSocket = serverFactory.createServerSocket(0)
        assertNotNull(serverSocket)
        assertTrue(serverSocket.isBound())
        assertTrue(serverSocket.getLocalPort() > 0)
        serverSocket.close()
    }
}
