package io.github.taetae98coding.diary.compose.map.web

import io.github.taetae98coding.diary.compose.map.naver.NaverMapResourceLoader
import io.github.taetae98coding.diary.compose.map.naver.installNaverMapRoutes
import io.github.taetae98coding.diary.compose.map.naver.naverMapNcpKeyId
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

// 지도마다 서버를 띄우면 화면에 들어갈 때마다 소켓을 열고 Ktor를 불러오므로, 앱이 시작할 때 하나만 띄우고 지도마다 문서를 등록해 쓴다.
public class MapHttpServer internal constructor(
    private val naverMapResourceLoader: NaverMapResourceLoader,
) {
    private val pageMap = ConcurrentHashMap<String, String>()

    @Volatile
    private var server: EmbeddedServer<*, *>? = null

    @Volatile
    private var rootUrl: String? = null

    public constructor() : this(naverMapResourceLoader = NaverMapResourceLoader(ncpKeyId = naverMapNcpKeyId()))

    // 화면을 그리기 전에 main 함수에서 부른다. 포트를 0으로 열어 운영체제가 비어 있는 포트를 고르게 한다.
    public fun start() {
        if (server != null) return

        val embeddedServer =
            embeddedServer(
                factory = CIO,
                host = LOOPBACK_HOST,
                port = ANY_PORT,
            ) {
                routing {
                    get(PAGE_PATH) {
                        val html = call.request.queryParameters[PAGE_ID_PARAMETER]?.let(pageMap::get)

                        if (html == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            call.response.header(HttpHeaders.CacheControl, "no-store")
                            call.respondText(html, ContentType.Text.Html)
                        }
                    }
                    installNaverMapRoutes(resourceLoader = naverMapResourceLoader)
                }
            }

        embeddedServer.start(wait = false)
        val port =
            runBlocking {
                embeddedServer.engine
                    .resolvedConnectors()
                    .single()
                    .port
            }

        rootUrl = "http://localhost:$port"
        server = embeddedServer
    }

    public fun stop() {
        server?.stop()
        server = null
        rootUrl = null
        pageMap.clear()
    }

    internal fun register(html: String): MapPage {
        val url = checkNotNull(rootUrl) { "MapHttpServer is not started." }
        val id = Uuid.random().toString()

        pageMap[id] = html

        return MapPage(url = "$url$PAGE_PATH?$PAGE_ID_PARAMETER=$id", onClose = { pageMap.remove(id) })
    }
}

internal class MapPage(
    val url: String,
    private val onClose: () -> Unit,
) : AutoCloseable {
    override fun close() {
        onClose()
    }
}

private const val LOOPBACK_HOST = "127.0.0.1"
private const val ANY_PORT = 0
private const val PAGE_PATH = "/map"
private const val PAGE_ID_PARAMETER = "id"
