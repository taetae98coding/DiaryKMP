package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.setting.repository.ChromeSessionImportSettingRepository
import org.koin.core.annotation.Factory

@Factory
public class UnselectChromeSessionProfileUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        chromeSessionImportSettingRepository.unsetProfileDirectory()
    }
}
