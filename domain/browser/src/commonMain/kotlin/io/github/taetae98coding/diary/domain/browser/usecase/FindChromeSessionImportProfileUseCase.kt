package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import io.github.taetae98coding.diary.domain.browser.repository.ChromeSessionImportSettingRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class FindChromeSessionImportProfileUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
    private val chromeProfileRepository: ChromeProfileRepository,
) : UseCase<Unit, ChromeProfile?>() {
    override suspend fun execute(parameter: Unit): ChromeProfile? {
        if (!chromeSessionImportSettingRepository.isSupported) return null

        val directory = chromeSessionImportSettingRepository.getProfileDirectory().first()

        return directory
            .takeIf { selected -> selected.isNotEmpty() }
            ?.let { selected -> findListedProfile(directory = selected) }
    }

    private suspend fun findListedProfile(directory: String): ChromeProfile? =
        chromeProfileRepository
            .findAll()
            .firstOrNull { profile -> profile.directory == directory }
}
