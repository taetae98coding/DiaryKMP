package io.github.taetae98coding.diary.compose.map.web

import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal class MapHttpServer private constructor(
    private val server: EmbeddedServer<*, *>,
    val url: String,
) {
    suspend fun close() {
        server.stopSuspend()
    }

    companion object {
        suspend fun start(routes: Routing.() -> Unit): MapHttpServer {
            val server =
                embeddedServer(
                    factory = CIO,
                    host = LOOPBACK_HOST,
                    port = ANY_PORT,
                ) {
                    routing(routes)
                }

            return try {
                server.startSuspend(wait = false)
                val port =
                    server.engine
                        .resolvedConnectors()
                        .single()
                        .port

                MapHttpServer(
                    server = server,
                    url = "http://localhost:$port/",
                )
            } catch (throwable: Throwable) {
                // 취소로 실패했어도 이미 뜬 서버는 닫아야 하므로 정리는 취소 없이 끝까지 실행한다.
                withContext(NonCancellable) { server.stopSuspend() }
                throw throwable
            }
        }
    }
}

private const val LOOPBACK_HOST = "127.0.0.1"
private const val ANY_PORT = 0
