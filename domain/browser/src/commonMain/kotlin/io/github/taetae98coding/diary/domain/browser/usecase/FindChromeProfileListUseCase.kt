package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class FindChromeProfileListUseCase internal constructor(
    private val chromeProfileRepository: ChromeProfileRepository,
) : UseCase<Unit, List<ChromeProfile>>() {
    override suspend fun execute(parameter: Unit): List<ChromeProfile> = chromeProfileRepository.findAll()
}
