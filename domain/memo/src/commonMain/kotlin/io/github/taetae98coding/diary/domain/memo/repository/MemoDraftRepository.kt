package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.memo.MemoDraftGeneration
import io.github.taetae98coding.diary.core.model.memo.MemoDraftRequest
import kotlinx.coroutines.flow.Flow

public interface MemoDraftRepository {
    public fun stream(
        setting: GeminiSetting,
        request: MemoDraftRequest,
    ): Flow<MemoDraftGeneration>
}
