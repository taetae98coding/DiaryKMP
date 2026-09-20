package io.github.taetae98coding.diary.data.setting.mapper

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.model.map.MapProvider

internal fun MapProvider.toLocal(): MapProviderLocalEntity =
    when (this) {
        MapProvider.NAVER -> MapProviderLocalEntity.NAVER
        MapProvider.GOOGLE -> MapProviderLocalEntity.GOOGLE
    }

internal fun MapProviderLocalEntity.toDomain(): MapProvider =
    when (this) {
        MapProviderLocalEntity.NAVER -> MapProvider.NAVER
        MapProviderLocalEntity.GOOGLE -> MapProvider.GOOGLE
    }
