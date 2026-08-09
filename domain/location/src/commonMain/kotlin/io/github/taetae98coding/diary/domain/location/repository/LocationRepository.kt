package io.github.taetae98coding.diary.domain.location.repository

import io.github.taetae98coding.diary.core.model.location.Coordinate

public interface LocationRepository {
    public suspend fun fetch(): Coordinate
}
