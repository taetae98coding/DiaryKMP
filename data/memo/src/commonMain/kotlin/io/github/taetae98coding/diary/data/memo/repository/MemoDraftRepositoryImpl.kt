package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraftGeneration
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.data.memo.mapper.memoDraftResponseJsonSchema
import io.github.taetae98coding.diary.data.memo.mapper.toMemoDraft
import io.github.taetae98coding.diary.data.memo.mapper.toMemoDraftProgress
import io.github.taetae98coding.diary.data.memo.mapper.toPrompt
import io.github.taetae98coding.diary.domain.memo.repository.MemoDraftRepository
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.domain.setting.exception.GeminiRequestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.annotation.Factory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Factory
internal class MemoDraftRepositoryImpl(
    private val geminiContentRemoteDataSource: GeminiContentRemoteDataSource,
) : MemoDraftRepository {
    override fun stream(
        setting: GeminiSetting,
        request: MemoDraftRequest,
    ): Flow<MemoDraftGeneration> =
        flow {
            val text = StringBuilder()
            var lastProgress = MemoDraftProgress.EMPTY

            geminiContentRemoteDataSource
                .streamStructuredContent(
                    apiKey = setting.apiKey,
                    model = setting.model,
                    systemInstruction = setting.systemPrompt,
                    prompt = request.toPrompt(),
                    responseJsonSchema = memoDraftResponseJsonSchema,
                    idleTimeout = IDLE_TIMEOUT,
                ).collect { chunk ->
                    text.append(chunk)

                    val progress = text.toString().toMemoDraftProgress()
                    if (progress != lastProgress) {
                        lastProgress = progress
                        emit(MemoDraftGeneration.InProgress(progress = progress))
                    }
                }

            emit(MemoDraftGeneration.Completed(draft = text.toString().toStructuredContent().toMemoDraft()))
        }.catch { cause -> throw cause.toDomainException() }

    private fun String.toStructuredContent(): JsonObject =
        try {
            json.parseToJsonElement(this) as? JsonObject
        } catch (_: IllegalArgumentException) {
            null
        } ?: throw GeminiException.InvalidContent(detail = "joined content is not a JSON object")

    // 실패 이유를 화면이 나눌 수 있게 도메인 예외로 옮기고, 원래 예외는 원인으로 남겨 오류 보고에서 응답의 종료 사유를 볼 수 있게 한다.
    private fun Throwable.toDomainException(): Throwable =
        when (this) {
            is GeminiException.InvalidApiKey -> GeminiApiKeyInvalidException(cause = this)
            is GeminiException.QuotaExceeded -> GeminiRequestException.QuotaExceeded(cause = this)
            is GeminiException.ModelNotFound -> GeminiRequestException.ModelNotFound(cause = this)
            is GeminiException.Blocked -> GeminiRequestException.Blocked(cause = this)
            is GeminiException.Truncated -> GeminiRequestException.Truncated(cause = this)
            is GeminiException.Timeout -> GeminiRequestException.Timeout(cause = this)
            is GeminiException.Network -> GeminiRequestException.Network(cause = this)
            is GeminiException.Server -> GeminiRequestException.Server(cause = this)
            is GeminiException.Other -> GeminiRequestException.Other(description = description, cause = this)
            is GeminiException.InvalidContent -> GeminiRequestException.Other(description = "", cause = this)
            else -> this
        }

    private companion object {
        private val IDLE_TIMEOUT: Duration = 2.minutes
        private val json: Json = Json
    }
}
