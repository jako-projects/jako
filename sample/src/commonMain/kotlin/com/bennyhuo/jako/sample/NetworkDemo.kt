package com.bennyhuo.jako.sample

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

object NetworkDemo {

    fun runEcho(message: String): String {
        ServerSocket(0, 50, InetAddress.getByName("127.0.0.1")).use { server ->
            val port = server.localPort
            var serverError: Throwable? = null

            val acceptThread = thread(start = true, name = "jako-sample-echo-server") {
                try {
                    server.accept().use { client ->
                        val reader = BufferedReader(InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                        val writer = PrintWriter(OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)
                        val line = reader.readLine()
                        writer.println("ECHO:$line")
                    }
                } catch (t: Throwable) {
                    serverError = t
                }
            }

            val response = Socket("127.0.0.1", port).use { socket ->
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                writer.println(message)
                reader.readLine()
            }

            acceptThread.join()
            serverError?.let { throw it }
            return response ?: ""
        }
    }
}
