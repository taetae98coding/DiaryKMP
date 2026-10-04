package io.github.taetae98coding.diary.domain.integrity.repository

import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict

public interface PlayIntegrityRepository {
    public suspend fun fetch(): PlayIntegrityVerdict?
}
