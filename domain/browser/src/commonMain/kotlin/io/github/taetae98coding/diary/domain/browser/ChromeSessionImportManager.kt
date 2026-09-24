package io.github.taetae98coding.diary.domain.browser

import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import kotlinx.coroutines.flow.Flow

public interface ChromeSessionImportManager {
    public val state: Flow<ChromeSessionImportState>

    public fun requestImport(clearsBefore: Boolean)
}
