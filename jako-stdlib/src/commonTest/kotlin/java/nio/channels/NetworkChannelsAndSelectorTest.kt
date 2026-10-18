package jako.test.nio

import java.net.InetSocketAddress
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NetworkChannelsAndSelectorTest {

    @Test
    fun testSocketChannelAndServerSocketChannelBlocking() {
        ServerSocketChannel.open().use { server ->
            assertTrue(server.isOpen())
            assertTrue(server.isBlocking())
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            assertEquals(true, server.getOption(StandardSocketOptions.SO_REUSEADDR))
            assertTrue(server.supportedOptions().contains(StandardSocketOptions.SO_REUSEADDR))

            server.bind(InetSocketAddress("127.0.0.1", 0))
            val serverAddr = server.getLocalAddress() as InetSocketAddress
            assertTrue(serverAddr.getPort() > 0)

            assertFailsWith<AlreadyBoundException> {
                server.bind(InetSocketAddress("127.0.0.1", 0))
            }

            SocketChannel.open().use { client ->
                assertFalse(client.isConnected())
                assertTrue(client.connect(InetSocketAddress("127.0.0.1", serverAddr.getPort())))
                assertTrue(client.isConnected())
                assertNotNull(client.getLocalAddress())
                assertNotNull(client.getRemoteAddress())

                server.accept()!!.use { accepted ->
                    assertTrue(accepted.isConnected())

                    // Scatter/Gather write from client to accepted server channel
                    val b1 = ByteBuffer.wrap("Hello ".encodeToByteArray())
                    val b2 = ByteBuffer.wrap("NIO!".encodeToByteArray())
                    val written = client.write(arrayOf(b1, b2))
                    assertEquals(10L, written)

                    val r1 = ByteBuffer.allocate(6)
                    val r2 = ByteBuffer.allocate(4)
                    var totalRead = 0L
                    while (totalRead < 10L) {
                        val n = accepted.read(arrayOf(r1, r2))
                        if (n <= 0L) break
                        totalRead += n
                    }
                    assertEquals(10L, totalRead)
                    r1.flip()
                    r2.flip()
                    val bytes1 = ByteArray(6).also { r1.get(it) }
                    val bytes2 = ByteArray(4).also { r2.get(it) }
                    assertEquals("Hello ", bytes1.decodeToString())
                    assertEquals("NIO!", bytes2.decodeToString())

                    // Test shutdownOutput -> EOF (-1) on peer read
                    client.shutdownOutput()
                    val eofBuf = ByteBuffer.allocate(8)
                    assertEquals(-1, accepted.read(eofBuf))
                }
            }
        }
    }

    @Test
    fun testNonBlockingAcceptAndExceptions() {
        ServerSocketChannel.open().use { unmarkedServer ->
            assertFailsWith<NotYetBoundException> {
                unmarkedServer.accept()
            }
        }

        SocketChannel.open().use { unconnectedClient ->
            assertFailsWith<NotYetConnectedException> {
                unconnectedClient.read(ByteBuffer.allocate(4))
            }
            assertFailsWith<NotYetConnectedException> {
                unconnectedClient.write(ByteBuffer.wrap(byteArrayOf(1)))
            }
        }

        ServerSocketChannel.open().use { server ->
            server.bind(InetSocketAddress("127.0.0.1", 0))
            server.configureBlocking(false)
            assertFalse(server.isBlocking())
            // Non-blocking accept with no pending connection must return null immediately
            assertNull(server.accept())
        }
    }

    @Test
    fun testDatagramChannelSendReceiveAndConnectedMode() {
        DatagramChannel.open().use { receiver ->
            receiver.bind(InetSocketAddress("127.0.0.1", 0))
            val recvAddr = receiver.getLocalAddress() as InetSocketAddress

            DatagramChannel.open().use { sender ->
                sender.bind(InetSocketAddress("127.0.0.1", 0))
                val payload = "UDP-Datagram-Payload".encodeToByteArray()
                val sent = sender.send(ByteBuffer.wrap(payload), InetSocketAddress("127.0.0.1", recvAddr.getPort()))
                assertEquals(payload.size, sent)

                val recvBuf = ByteBuffer.allocate(64)
                val fromAddr = receiver.receive(recvBuf) as InetSocketAddress
                assertNotNull(fromAddr)
                recvBuf.flip()
                val receivedBytes = ByteArray(recvBuf.remaining()).also { recvBuf.get(it) }
                assertEquals("UDP-Datagram-Payload", receivedBytes.decodeToString())

                // Connected DatagramChannel read/write
                sender.connect(InetSocketAddress("127.0.0.1", recvAddr.getPort()))
                assertTrue(sender.isConnected())
                assertEquals(4, sender.write(ByteBuffer.wrap("PING".encodeToByteArray())))

                val pingBuf = ByteBuffer.allocate(16)
                receiver.receive(pingBuf)
                pingBuf.flip()
                val pingBytes = ByteArray(pingBuf.remaining()).also { pingBuf.get(it) }
                assertEquals("PING", pingBytes.decodeToString())

                sender.disconnect()
                assertFalse(sender.isConnected())
            }
        }
    }

    @Test
    fun testSelectorWithPipeAndSocketChannels() {
        Selector.open().use { selector ->
            assertTrue(selector.isOpen())

            // 1. Test Selector with Pipe
            val pipe = Pipe.open()
            val source = pipe.source()
            val sink = pipe.sink()
            try {
                source.configureBlocking(false)
                sink.configureBlocking(false)

                val readKey = source.register(selector, SelectionKey.OP_READ, "pipe-source")
                assertSame(source, readKey.channel())
                assertSame(selector, readKey.selector())
                assertEquals(SelectionKey.OP_READ, readKey.interestOps())
                assertEquals("pipe-source", readKey.attachment())

                // Nothing written yet -> selectNow should return 0
                assertEquals(0, selector.selectNow())

                // Write to sink -> source becomes readable
                sink.write(ByteBuffer.wrap("PipeSelector".encodeToByteArray()))
                val readyCount = selector.select(1000L)
                assertTrue(readyCount >= 1)
                assertTrue(selector.selectedKeys().contains(readKey))
                assertTrue(readKey.isReadable())

                val dst = ByteBuffer.allocate(32)
                val n = source.read(dst)
                assertEquals(12, n)
                selector.selectedKeys().clear()

                readKey.cancel()
                selector.selectNow()
                assertFalse(readKey.isValid())
                assertFailsWith<CancelledKeyException> {
                    readKey.interestOps()
                }
            } finally {
                source.close()
                sink.close()
            }

            // 2. Test Selector with ServerSocketChannel & SocketChannel
            ServerSocketChannel.open().use { server ->
                server.bind(InetSocketAddress("127.0.0.1", 0))
                server.configureBlocking(false)
                val port = (server.getLocalAddress() as InetSocketAddress).getPort()

                val acceptKey = server.register(selector, SelectionKey.OP_ACCEPT)
                assertEquals(0, selector.selectNow())

                SocketChannel.open(InetSocketAddress("127.0.0.1", port)).use { client ->
                    val acceptedReady = selector.select(1000L)
                    assertTrue(acceptedReady >= 1)
                    assertTrue(acceptKey.isAcceptable())
                    selector.selectedKeys().clear()

                    server.accept()!!.use { serverConn ->
                        serverConn.configureBlocking(false)
                        val connKey = serverConn.register(selector, SelectionKey.OP_READ)

                        client.write(ByteBuffer.wrap("SelectMsg".encodeToByteArray()))
                        val readReady = selector.select(1000L)
                        assertTrue(readReady >= 1)
                        assertTrue(connKey.isReadable())

                        val buf = ByteBuffer.allocate(16)
                        assertEquals(9, serverConn.read(buf))
                        buf.flip()
                        val data = ByteArray(buf.remaining()).also { buf.get(it) }
                        assertEquals("SelectMsg", data.decodeToString())
                    }
                }
            }

            // 3. Test wakeup()
            selector.wakeup()
            assertEquals(0, selector.select(1000L))
        }
    }
}
