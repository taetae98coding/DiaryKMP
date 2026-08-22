package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.setting.repository.GeminiSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetGeminiSettingUseCase internal constructor(
    private val geminiSettingRepository: GeminiSettingRepository,
) : FlowUseCase<Unit, GeminiSetting>() {
    override fun execute(parameter: Unit): Flow<Result<GeminiSetting>> =
        geminiSettingRepository
            .get()
            .map { setting -> Result.success(setting) }
}
