package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest

public interface MemoDraftRepository {
    public suspend fun fetch(
        setting: GeminiSetting,
        request: MemoDraftRequest,
    ): MemoDraft
}
