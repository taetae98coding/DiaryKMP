package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class FindChromeSessionImportSupportUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean = chromeSessionImportSettingRepository.isSupported
}
