package io.github.taetae98coding.diary.core.location.api

public interface LocationProvider {
    public suspend fun getCurrentLocation(): Location?
}
