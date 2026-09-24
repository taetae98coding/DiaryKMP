package io.github.taetae98coding.diary.compose.map.naver

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.web.CAMERA_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.compose.map.web.PINS_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_MARKER_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.pinMarkerToScriptValue
import io.github.taetae98coding.diary.compose.map.web.toScriptValue
import io.ktor.http.ContentType
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.takeFrom
import io.ktor.http.toURI
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.requireQueryParameter
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import java.net.URLConnection

internal object NaverMapHttpServer {
    suspend fun start(
        camera: DiaryMapCamera?,
        spot: DiaryMapCoordinate? = null,
        isSpotSelectable: Boolean = false,
        pins: List<DiaryMapPin> = emptyList(),
        isPinSelectable: Boolean = false,
    ): MapHttpServer? =
        start(
            ncpKeyId =
                System
                    .getProperty(NAVER_MAP_NCP_KEY_ID_PROPERTY)
                    .orEmpty(),
            camera = camera,
            spot = spot,
            isSpotSelectable = isSpotSelectable,
            pins = pins,
            isPinSelectable = isPinSelectable,
        )

    suspend fun start(
        ncpKeyId: String,
        camera: DiaryMapCamera?,
        spot: DiaryMapCoordinate? = null,
        isSpotSelectable: Boolean = false,
        pins: List<DiaryMapPin> = emptyList(),
        isPinSelectable: Boolean = false,
    ): MapHttpServer? {
        val html =
            createNaverMapHtml(
                ncpKeyId = ncpKeyId,
                camera = camera,
                spot = spot,
                isSpotSelectable = isSpotSelectable,
                pins = pins,
                isPinSelectable = isPinSelectable,
            ) ?: return null
        val resourceLoader = NaverMapResourceLoader(ncpKeyId)
        return start(
            html = html,
            naverMapsSdkLoader = resourceLoader::loadSdk,
            naverMapsProxyLoader = resourceLoader::loadProxy,
        )
    }

    suspend fun start(
        html: String,
        naverMapsSdkLoader: () -> String,
        naverMapsProxyLoader: (String) -> String,
    ): MapHttpServer =
        MapHttpServer.start {
            get("/") {
                call.response.header(HttpHeaders.CacheControl, "no-store")
                call.respondText(html, ContentType.Text.Html)
            }
            get(NAVER_MAPS_SDK_PATH) {
                call.respondJavaScript(naverMapsSdkLoader)
            }
            get(NAVER_MAPS_PROXY_PATH) {
                call.respondJavaScript {
                    naverMapsProxyLoader(call.requireQueryParameter(PROXY_URL_PARAMETER))
                }
            }
        }
}

internal fun createNaverMapHtml(
    ncpKeyId: String,
    camera: DiaryMapCamera?,
    spot: DiaryMapCoordinate? = null,
    isSpotSelectable: Boolean = false,
    pins: List<DiaryMapPin> = emptyList(),
    isPinSelectable: Boolean = false,
): String? {
    val normalizedKey = ncpKeyId.trim()
    if (!normalizedKey.matches(NCP_KEY_ID_PATTERN)) {
        return null
    }

    return NAVER_MAP_HTML_TEMPLATE
        .replace(NCP_KEY_ID_PLACEHOLDER, normalizedKey)
        .replace(CAMERA_PLACEHOLDER, camera.toScriptValue())
        .replace(SPOT_PLACEHOLDER, spot.toScriptValue())
        .replace(SPOT_SELECTABLE_PLACEHOLDER, isSpotSelectable.toScriptValue())
        .replace(PINS_PLACEHOLDER, pins.toScriptValue())
        .replace(PIN_SELECTABLE_PLACEHOLDER, isPinSelectable.toScriptValue())
        .replace(PIN_MARKER_PLACEHOLDER, pinMarkerToScriptValue())
}

internal class NaverMapResourceLoader(
    private val ncpKeyId: String,
) {
    private val sdk: String by lazy {
        val url =
            URLBuilder()
                .takeFrom(NAVER_MAPS_SDK_URL)
                .apply {
                    parameters.append(NCP_KEY_ID_PARAMETER, ncpKeyId)
                }.build()
        val source =
            download(url)
                .toString(Charsets.UTF_8)
        transformNaverMapsSdk(source)
    }

    fun loadSdk(): String = sdk

    fun loadProxy(url: String): String =
        download(createNaverMapProxyUrl(url))
            .toString(Charsets.UTF_8)

    private fun download(url: Url): ByteArray {
        val connection =
            url
                .toURI()
                .toURL()
                .openConnection()
                .applySdkTimeouts()
        return connection.getInputStream().use { it.readBytes() }
    }
}

internal fun createNaverMapProxyUrl(url: String): Url =
    URLBuilder()
        .takeFrom(url)
        .apply {
            require(host in NAVER_MAPS_PROXY_HOSTS)
            require(protocol == URLProtocol.HTTP || protocol == URLProtocol.HTTPS)
            require(port == DEFAULT_PORT || port == HTTP_PORT || port == HTTPS_PORT)
            require(user == null && password == null)
            require(fragment.isEmpty())

            protocol = URLProtocol.HTTPS
            port = DEFAULT_PORT
        }.build()

internal fun transformNaverMapsSdk(source: String): String =
    source
        .replaceRequired(
            oldValue = NAVER_MAPS_JSONP_EXPRESSION,
            newValue =
                "fetch(\"$NAVER_MAPS_PROXY_PATH?url=\"+encodeURIComponent(e))" +
                    ".then(function(r){if(!r.ok)throw new Error();" +
                    "return r.text()})" +
                    ".then(function(code){(0,eval)(code)})" +
                    ".catch(u)",
        ).replaceRequired(
            oldValue = NAVER_MAPS_SECURE_CONTEXT_EXPRESSION,
            newValue = "et=true",
        )

private fun String.replaceRequired(
    oldValue: String,
    newValue: String,
): String {
    require(oldValue in this) { "Unsupported Naver Maps SDK" }
    return replace(oldValue = oldValue, newValue = newValue)
}

private fun URLConnection.applySdkTimeouts(): URLConnection =
    apply {
        connectTimeout = SDK_REQUEST_TIMEOUT_MILLIS
        readTimeout = SDK_REQUEST_TIMEOUT_MILLIS
    }

private suspend fun ApplicationCall.respondJavaScript(loader: () -> String) {
    response.header(HttpHeaders.CacheControl, "no-store")
    val javascript =
        try {
            loader()
        } catch (_: Throwable) {
            respond(HttpStatusCode.BadGateway)
            return
        }
    respondText(javascript, ContentType.Text.JavaScript)
}

private const val NAVER_MAPS_SDK_PATH = "/naver-maps.js"
private const val NAVER_MAPS_PROXY_PATH = "/naver-proxy"
private const val NAVER_MAPS_SDK_URL = "https://oapi.map.naver.com/openapi/v3/maps.js"
private const val NAVER_MAPS_JSONP_EXPRESSION = "o.src=e"
private const val NAVER_MAPS_SECURE_CONTEXT_EXPRESSION =
    "et=tt||0===t.location.protocol.indexOf(\"https\")"
private const val NAVER_MAP_NCP_KEY_ID_PROPERTY =
    "io.github.taetae98coding.diary.naverMapNcpKeyId"
private const val PROXY_URL_PARAMETER = "url"
private const val NCP_KEY_ID_PARAMETER = "ncpKeyId"
private const val NCP_KEY_ID_PLACEHOLDER = "{{NAVER_MAP_NCP_KEY_ID}}"
private const val SDK_REQUEST_TIMEOUT_MILLIS = 15_000
private const val HTTP_PORT = 80
private const val HTTPS_PORT = 443
private val NCP_KEY_ID_PATTERN = Regex("[A-Za-z0-9_-]+")
private val NAVER_MAP_HTML_TEMPLATE: String by lazy {
    checkNotNull(NaverMapHttpServer::class.java.getResourceAsStream("/naver-map.html"))
        .bufferedReader()
        .use { it.readText() }
}
private val NAVER_MAPS_PROXY_HOSTS =
    setOf(
        "oapi.map.naver.com",
        "map.pstatic.net",
        "nrbe.pstatic.net",
    )
