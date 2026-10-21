package io.github.taetae98coding.diary.feature.login.ui.credential

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

// RFC 8252 7.3: 데스크톱 로그인은 루프백 IP의 빈 포트에서 인가 응답을 한 번 받는다.
// Google은 이 주소로 바로 돌아오고, Apple은 https 복귀 주소만 받으므로 서버 콜백이 이 주소로 다시 보내 준다.
internal class LoopbackRedirectReceiver(
    private val locale: Locale = Locale.getDefault(),
) : AutoCloseable {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(LOOPBACK_HOST, EPHEMERAL_PORT), 0)
    private val parametersQueue: BlockingQueue<Map<String, String>> = ArrayBlockingQueue(1)

    val redirectUri: String
        get() = "http://$LOOPBACK_HOST:${server.address.port}$CALLBACK_PATH"

    init {
        server.createContext(CALLBACK_PATH, ::handle)
        server.start()
    }

    fun waitForParameters(): Map<String, String> = parametersQueue.take()

    override fun close() {
        server.stop(0)
    }

    private fun handle(exchange: HttpExchange) {
        val parameters =
            exchange.requestURI.rawQuery
                .orEmpty()
                .decodeUrlQuery()
        val body = landingPage().encodeToByteArray()

        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.sendResponseHeaders(HTTP_OK, body.size.toLong())
        exchange.responseBody.use { stream -> stream.write(body) }
        exchange.close()

        parametersQueue.offer(parameters)
    }

    private fun landingPage(): String {
        val message =
            if (locale.language == Locale.KOREAN.language) {
                "이 창을 닫고 앱으로 돌아가세요."
            } else {
                "You can close this window and return to the app."
            }

        return """<!doctype html><html lang="${locale.language}"><head><meta charset="utf-8"><title>Diary</title></head><body><p>$message</p></body></html>"""
    }

    private companion object {
        private const val LOOPBACK_HOST = "127.0.0.1"
        private const val CALLBACK_PATH = "/callback"
        private const val EPHEMERAL_PORT = 0
        private const val HTTP_OK = 200
    }
}
