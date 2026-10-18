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
    fun testUdpLoopbackSendAndReceive() {
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
}
