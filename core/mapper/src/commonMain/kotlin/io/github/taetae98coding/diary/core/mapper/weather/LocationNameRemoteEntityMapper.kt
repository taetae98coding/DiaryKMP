package io.github.taetae98coding.diary.core.mapper.weather

import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity

private const val KOREAN_LANGUAGE_CODE = "ko"

public fun List<LocationNameRemoteEntity>.toLocationName(): String =
    firstOrNull()
        ?.let { remote -> remote.localNames[KOREAN_LANGUAGE_CODE] ?: remote.name }
        .orEmpty()
