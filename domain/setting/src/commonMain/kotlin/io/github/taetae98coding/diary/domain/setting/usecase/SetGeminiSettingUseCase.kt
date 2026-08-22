package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import org.koin.core.annotation.Factory

@Factory
public class SetGeminiSettingUseCase internal constructor(
    private val geminiSettingRepository: GeminiSettingRepository,
) : UseCase<GeminiSetting, Unit>() {
    override suspend fun execute(parameter: GeminiSetting) {
        geminiSettingRepository.upsert(setting = parameter)
    }
}
