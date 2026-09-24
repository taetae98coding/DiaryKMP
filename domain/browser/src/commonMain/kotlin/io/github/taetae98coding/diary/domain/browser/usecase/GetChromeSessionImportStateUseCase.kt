package io.github.taetae98coding.diary.domain.browser.usecase

import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.ChromeSessionImportManager
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetChromeSessionImportStateUseCase internal constructor(
    private val chromeSessionImportManager: ChromeSessionImportManager,
) : FlowUseCase<Unit, ChromeSessionImportState>() {
    override fun execute(parameter: Unit): Flow<Result<ChromeSessionImportState>> = chromeSessionImportManager.state.map { state -> Result.success(state) }
}
