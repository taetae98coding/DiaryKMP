package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.domain.browser.ChromeSessionImportManager
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class SelectChromeSessionProfileUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
    private val chromeSessionImportManager: ChromeSessionImportManager,
) : UseCase<String, Unit>() {
    override suspend fun execute(parameter: String) {
        chromeSessionImportSettingRepository.setProfileDirectory(directory = parameter)
        chromeSessionImportManager.requestImport(clearsBefore = true)
    }
}
