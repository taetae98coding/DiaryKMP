package io.github.taetae98coding.diary.domain.setting.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.GeminiModelRepository

@Inject
public class FetchGeminiModelUseCase internal constructor(
    private val geminiModelRepository: GeminiModelRepository,
) : UseCase<String, List<GeminiModel>>() {
    override suspend fun execute(parameter: String): List<GeminiModel> = geminiModelRepository.fetch(apiKey = parameter)
}
