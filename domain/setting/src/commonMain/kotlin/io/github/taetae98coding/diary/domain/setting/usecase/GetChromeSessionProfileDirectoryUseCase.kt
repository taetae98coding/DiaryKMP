package io.github.taetae98coding.diary.domain.setting.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.setting.repository.ChromeSessionImportSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetChromeSessionProfileDirectoryUseCase internal constructor(
    private val chromeSessionImportSettingRepository: ChromeSessionImportSettingRepository,
) : FlowUseCase<Unit, String>() {
    override fun execute(parameter: Unit): Flow<Result<String>> =
        chromeSessionImportSettingRepository
            .getProfileDirectory()
            .map { directory -> Result.success(directory) }
}
