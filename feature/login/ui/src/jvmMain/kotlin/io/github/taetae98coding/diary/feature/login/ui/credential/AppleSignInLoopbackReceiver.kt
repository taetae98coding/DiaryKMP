package io.github.taetae98coding.diary.feature.login.ui.credential

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

// Apple은 https 복귀 주소만 받으므로 서버 콜백이 로컬 주소로 다시 보내 주고, 그 한 번의 응답을 여기서 받는다.
internal class AppleSignInLoopbackReceiver(
    private val locale: Locale = Locale.getDefault(),
) : AutoCloseable {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(LOOPBACK_HOST, EPHEMERAL_PORT), 0)
    private val responses: BlockingQueue<AppleWebSignInResponse> = ArrayBlockingQueue(1)

    val returnUri: String
        get() = "http://$LOOPBACK_HOST:${server.address.port}$CALLBACK_PATH"

    init {
        server.createContext(CALLBACK_PATH, ::handle)
        server.start()
    }

    fun waitForResponse(): AppleWebSignInResponse = responses.take()

    override fun close() {
        server.stop(0)
    }

    private fun handle(exchange: HttpExchange) {
        val parameters =
            exchange.requestURI.rawQuery
                .orEmpty()
                .toQueryParameters()
        val body = landingPage().encodeToByteArray()

        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.sendResponseHeaders(HTTP_OK, body.size.toLong())
        exchange.responseBody.use { stream -> stream.write(body) }
        exchange.close()

        responses.offer(
            AppleWebSignInResponse(
                idToken = parameters[AppleWebSignInResponse.ID_TOKEN_PARAMETER],
                state = parameters[AppleWebSignInResponse.STATE_PARAMETER],
                error = parameters[AppleWebSignInResponse.ERROR_PARAMETER],
            ),
        )
    }

    private fun String.toQueryParameters(): Map<String, String> =
        split('&')
            .filter { it.isNotEmpty() }
            .associate { parameter ->
                val name = parameter.substringBefore('=')
                val value = parameter.substringAfter('=', missingDelimiterValue = "")

                URLDecoder.decode(name, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
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

    companion object {
        const val LOOPBACK_HOST: String = "127.0.0.1"
        const val CALLBACK_PATH: String = "/callback"
        private const val EPHEMERAL_PORT = 0
        private const val HTTP_OK = 200
    }
}
