package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.MemoDraftRepository
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock

@Factory
public class FetchMemoDraftUseCase internal constructor(
    private val geminiSettingRepository: GeminiSettingRepository,
    private val memoDraftRepository: MemoDraftRepository,
    private val clock: Clock,
) : UseCase<FetchMemoDraftUseCase.Parameter, MemoDraft>() {
    override suspend fun execute(parameter: Parameter): MemoDraft {
        val setting = geminiSettingRepository.get().first()
        val timeZone = TimeZone.currentSystemDefault()
        val request =
            MemoDraftRequest(
                prompt = parameter.prompt,
                title = parameter.title,
                description = parameter.description,
                dateTime = parameter.dateTime,
                now = clock.now().toLocalDateTime(timeZone),
                timeZone = timeZone,
            )

        return memoDraftRepository
            .fetch(setting = setting, request = request)
            .toUsable()
    }

    private fun MemoDraft.toUsable(): MemoDraft =
        MemoDraft(
            title = title.takeUnless { it.isBlank() }.orEmpty(),
            description = description.takeUnless { it.isBlank() }.orEmpty(),
            dateTime = dateTime?.takeIf { it.isUsable() },
        )

    private fun MemoDateTime.isUsable(): Boolean =
        when (this) {
            is MemoDateTime.AllDay -> !dateRange.isEmpty()
            is MemoDateTime.DateTime -> start <= endInclusive
        }

    public data class Parameter(
        val prompt: String,
        val title: String,
        val description: String,
        val dateTime: MemoDateTime?,
    )
}
