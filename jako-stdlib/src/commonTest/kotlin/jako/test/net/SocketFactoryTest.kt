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

        if (java.lang.System.getProperty("os.name") == "WASI") {
            val ex = assertFailsWith<UnsupportedOperationException> {
                serverFactory.createServerSocket(0)
            }
            assertTrue(ex.message!!.contains("is not supported on target 'WasmWasi'"))
            return
        }

        val serverSocket = serverFactory.createServerSocket(0)
        assertNotNull(serverSocket)
        assertTrue(serverSocket.isBound())
        assertTrue(serverSocket.getLocalPort() > 0)
        serverSocket.close()
    }
}
