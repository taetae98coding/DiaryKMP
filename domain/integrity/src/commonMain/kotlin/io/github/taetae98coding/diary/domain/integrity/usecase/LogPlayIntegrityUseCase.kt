package io.github.taetae98coding.diary.domain.integrity.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.integrity.repository.PlayIntegrityRepository
import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger

@Inject
public class LogPlayIntegrityUseCase internal constructor(
    private val playIntegrityRepository: PlayIntegrityRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val verdict = playIntegrityRepository.fetch() ?: return

        DiaryLogger.log(log = AnalyticsEventLog(name = PLAY_INTEGRITY_EVENT, parameters = verdict.toFlatVerdict()))
    }

    private companion object {
        const val PLAY_INTEGRITY_EVENT: String = "play_integrity"
    }
}
