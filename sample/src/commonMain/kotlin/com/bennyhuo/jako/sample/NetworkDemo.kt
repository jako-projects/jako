package com.bennyhuo.jako.sample

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@WebSupport(WebRuntime.NODE_JS, reason = "Loopback TCP echo server requires Node.js socket support")
object NetworkDemo {

    fun runEcho(message: String): String {
        ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")).use { server ->
            val port = server.localPort
            var serverError: Throwable? = null
            val executor = Executors.newSingleThreadExecutor()

            try {
                val acceptFuture = executor.submit {
                    try {
                        server.accept().use { client ->
                            val reader = BufferedReader(InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                            val writer = PrintWriter(OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))
                            val line = reader.readLine()
                            writer.println("ECHO:$line")
                            writer.flush()
                        }
                    } catch (t: Throwable) {
                        serverError = t
                    }
                }

                val response = Socket("127.0.0.1", port).use { socket ->
                    val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                    writer.println(message)
                    writer.flush()
                    reader.readLine()
                }

                acceptFuture.get(10, TimeUnit.SECONDS)
                serverError?.let { throw it }
                return response ?: ""
            } finally {
                executor.shutdown()
            }
        }
    }
}
