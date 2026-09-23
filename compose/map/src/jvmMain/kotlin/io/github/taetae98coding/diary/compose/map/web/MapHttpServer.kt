package io.github.taetae98coding.diary.compose.map.web

import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking

internal class MapHttpServer private constructor(
    private val server: EmbeddedServer<*, *>,
    val url: String,
) : AutoCloseable {
    override fun close() {
        server.stop()
    }

    companion object {
        fun start(routes: Routing.() -> Unit): MapHttpServer {
            val server =
                embeddedServer(
                    factory = CIO,
                    host = LOOPBACK_HOST,
                    port = ANY_PORT,
                ) {
                    routing(routes)
                }

            return try {
                server.start()
                val port =
                    runBlocking {
                        server.engine
                            .resolvedConnectors()
                            .single()
                            .port
                    }

                MapHttpServer(
                    server = server,
                    url = "http://localhost:$port/",
                )
            } catch (throwable: Throwable) {
                server.stop()
                throw throwable
            }
        }
    }
}

private const val LOOPBACK_HOST = "127.0.0.1"
private const val ANY_PORT = 0
