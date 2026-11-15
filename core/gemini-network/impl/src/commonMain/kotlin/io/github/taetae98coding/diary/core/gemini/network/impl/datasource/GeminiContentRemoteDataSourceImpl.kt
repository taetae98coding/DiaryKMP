package io.github.taetae98coding.diary.core.gemini.network.impl.datasource

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.ContentRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerateContentRequestRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerateContentResponseRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.GenerationConfigRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.entity.PartRemoteEntity
import io.github.taetae98coding.diary.core.gemini.network.impl.toGeminiException
import io.github.taetae98coding.diary.library.ktor.reportUnreadableContent
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.LineEnding
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Factory
import kotlin.time.Duration

@Factory
internal class GeminiContentRemoteDataSourceImpl(
    @GeminiHttpClient
    private val httpClient: HttpClient,
    @GeminiJson
    private val json: Json,
) : GeminiContentRemoteDataSource {
    override fun streamStructuredContent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        prompt: String,
        responseJsonSchema: JsonObject,
        idleTimeout: Duration,
    ): Flow<String> =
        flow {
            val stopReason = StopReason()

            httpClient
                .preparePost("$model:$STREAM_GENERATE_CONTENT_METHOD") {
                    parameter(ALT_PARAMETER, SSE_ALT)
                    header(API_KEY_HEADER, apiKey)
                    // 실패 응답의 본문을 직접 읽어 실패 이유를 나누므로, 본문을 흘려 받는 요청에서 자동 실패 처리를 끈다.
                    expectSuccess = false
                    // 조각이 이어서 오는 동안에는 전체 시간을 제한하지 않고, 조각 사이의 대기만 제한한다.
                    timeout {
                        requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                        socketTimeoutMillis = idleTimeout.inWholeMilliseconds
                    }
                    setBody(
                        GenerateContentRequestRemoteEntity(
                            contents = listOf(prompt.toContent()),
                            generationConfig =
                                GenerationConfigRemoteEntity(
                                    responseMimeType = JSON_MIME_TYPE,
                                    responseJsonSchema = responseJsonSchema,
                                ),
                            systemInstruction = systemInstruction.takeIf { it.isNotBlank() }?.toContent(),
                        ),
                    )
                }.execute { response ->
                    if (!response.status.isSuccess()) {
                        throw toGeminiException(status = response.status, body = response.bodyAsText(), json = json, cause = null)
                    }

                    response.bodyAsChannel().forEachEventData { data -> emitText(data = data, stopReason = stopReason, response = response) }
                }

            stopReason.toAbnormalStopExceptionOrNull()?.let { exception -> throw exception }
        }.catch { cause -> throw cause.toConnectionExceptionOrSelf() }

    private fun String.toContent(): ContentRemoteEntity = ContentRemoteEntity(parts = listOf(PartRemoteEntity(text = this)))

    // 서버가 보내는 이벤트는 `data:` 줄들과 빈 줄로 끝나는 묶음이며, 한 묶음의 `data:` 줄을 이어 붙인 것이 응답 조각 하나다.
    // SSE는 CR 단독 줄 끝도 허용하므로 LF·CRLF만 받는 기본값 대신 Lenient로 읽는다.
    private suspend fun ByteReadChannel.forEachEventData(onData: suspend (String) -> Unit) {
        val data = StringBuilder()

        while (true) {
            val line = readLine(lineEnding = LineEnding.Lenient) ?: break

            when {
                line.isEmpty() && data.isNotEmpty() -> {
                    onData(data.toString())
                    data.clear()
                }

                line.startsWith(DATA_FIELD_PREFIX) -> data.append(line.removePrefix(DATA_FIELD_PREFIX).trimStart())
            }
        }

        if (data.isNotEmpty()) onData(data.toString())
    }

    private suspend fun FlowCollector<String>.emitText(
        data: String,
        stopReason: StopReason,
        response: HttpResponse,
    ) {
        val event =
            try {
                json.decodeFromString<GenerateContentResponseRemoteEntity>(data)
            } catch (exception: SerializationException) {
                response.reportUnreadableContent(cause = exception)
                // 조각에는 생성된 내용이 담기므로 파싱 예외를 원인으로 잇지 않는다.
                throw GeminiException.InvalidContent(detail = "stream event could not be read")
            }
        val candidate = event.candidates.firstOrNull()

        event.promptFeedback
            ?.blockReason
            ?.takeIf { it.isNotEmpty() }
            ?.let { reason -> stopReason.blockReason = reason }
        candidate
            ?.finishReason
            ?.takeIf { it.isNotEmpty() }
            ?.let { reason -> stopReason.finishReason = reason }

        val text =
            candidate
                ?.content
                ?.parts
                ?.filterNot { part -> part.isThought }
                ?.joinToString(separator = "") { part -> part.text }
                .orEmpty()

        if (text.isNotEmpty()) emit(text)
    }

    private fun Throwable.toConnectionExceptionOrSelf(): Throwable =
        when (this) {
            is GeminiException -> this
            is HttpRequestTimeoutException, is SocketTimeoutException -> GeminiException.Timeout(cause = this)
            is IOException -> GeminiException.Network(cause = this)
            else -> this
        }

    private class StopReason {
        var blockReason: String = ""
        var finishReason: String = ""

        fun toAbnormalStopExceptionOrNull(): GeminiException? {
            val finishDetail = "$FINISH_REASON_DETAIL_PREFIX$finishReason"

            return when {
                blockReason.isNotEmpty() -> GeminiException.Blocked(detail = "$BLOCK_REASON_DETAIL_PREFIX$blockReason")
                finishReason in BLOCKED_FINISH_REASON_SET -> GeminiException.Blocked(detail = finishDetail)
                finishReason == MAX_TOKENS_FINISH_REASON -> GeminiException.Truncated(detail = finishDetail)
                finishReason.isNotEmpty() && finishReason != STOP_FINISH_REASON -> GeminiException.InvalidContent(detail = finishDetail)
                else -> null
            }
        }
    }

    private companion object {
        private const val STREAM_GENERATE_CONTENT_METHOD = "streamGenerateContent"
        private const val ALT_PARAMETER = "alt"
        private const val SSE_ALT = "sse"
        private const val DATA_FIELD_PREFIX = "data:"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val JSON_MIME_TYPE = "application/json"
        private const val FINISH_REASON_DETAIL_PREFIX = "finishReason="
        private const val BLOCK_REASON_DETAIL_PREFIX = "blockReason="
        private const val STOP_FINISH_REASON = "STOP"
        private const val MAX_TOKENS_FINISH_REASON = "MAX_TOKENS"
        private val BLOCKED_FINISH_REASON_SET: Set<String> = setOf("SAFETY", "BLOCKLIST", "PROHIBITED_CONTENT", "SPII")
    }
}
