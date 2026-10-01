package com.hippi345.nowplayingwallpaper.spotify

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Receives Spotify's OAuth redirect on 127.0.0.1 inside the emulator/device.
 */
class SpotifyLoopbackRedirectServer(
    private val port: Int = SpotifyOAuthConfig.LOOPBACK_PORT,
    private val path: String = SpotifyOAuthConfig.LOOPBACK_PATH,
) {
    data class RedirectResult(
        val code: String?,
        val error: String?,
    )

    @Volatile
    private var serverSocket: ServerSocket? = null

    fun start() {
        stop()
        serverSocket = ServerSocket(port, 1, InetAddress.getByName(SpotifyOAuthConfig.LOOPBACK_HOST)).apply {
            soTimeout = ACCEPT_TIMEOUT_MS
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        } finally {
            serverSocket = null
        }
    }

    suspend fun awaitRedirect(): RedirectResult = withContext(Dispatchers.IO) {
        val socket = serverSocket ?: error("Redirect server not started")
        try {
            socket.accept().use { client -> handleClient(client) }
        } catch (_: SocketTimeoutException) {
            RedirectResult(code = null, error = "redirect_timeout")
        } finally {
            stop()
        }
    }

    private fun handleClient(client: Socket): RedirectResult {
        BufferedReader(InputStreamReader(client.getInputStream())).use { reader ->
            val requestLine = reader.readLine() ?: return RedirectResult(null, "invalid_redirect")
            if (!requestLine.startsWith("GET ")) {
                writeResponse(client, "Unsupported")
                return RedirectResult(null, "invalid_redirect")
            }
            val target = requestLine.split(" ").getOrNull(1) ?: ""
            val uri = Uri.parse("http://localhost$target")
            if (uri.path != path) {
                writeResponse(client, "Not found")
                return RedirectResult(null, "invalid_redirect")
            }
            writeResponse(client, "Signed in. Return to Now Playing Wallpaper.")
            return RedirectResult(
                code = uri.getQueryParameter("code"),
                error = uri.getQueryParameter("error"),
            )
        }
    }

    private fun writeResponse(client: Socket, message: String) {
        val body = "<html><body><p>$message</p></body></html>"
        val bytes = body.toByteArray(Charsets.UTF_8)
        val header = buildString {
            append("HTTP/1.1 200 OK\r\n")
            append("Content-Type: text/html; charset=utf-8\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        client.getOutputStream().use { out ->
            out.write(header.toByteArray(Charsets.UTF_8))
            out.write(bytes)
            out.flush()
        }
    }

    companion object {
        private const val ACCEPT_TIMEOUT_MS = 120_000
    }
}
