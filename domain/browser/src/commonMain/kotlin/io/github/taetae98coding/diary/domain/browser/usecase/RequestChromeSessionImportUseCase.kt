package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.domain.browser.ChromeSessionImportManager
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class RequestChromeSessionImportUseCase internal constructor(
    private val chromeSessionImportManager: ChromeSessionImportManager,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        chromeSessionImportManager.requestImport(clearsBefore = false)
    }
}
