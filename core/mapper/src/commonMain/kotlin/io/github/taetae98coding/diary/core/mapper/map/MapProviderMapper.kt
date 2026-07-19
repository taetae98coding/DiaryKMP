package io.github.taetae98coding.diary.core.mapper.map

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.model.map.MapProvider

public fun MapProvider.toLocal(): MapProviderLocalEntity =
    when (this) {
        MapProvider.NAVER -> MapProviderLocalEntity.NAVER
        MapProvider.GOOGLE -> MapProviderLocalEntity.GOOGLE
    }

public fun MapProviderLocalEntity.toDomain(): MapProvider =
    when (this) {
        MapProviderLocalEntity.NAVER -> MapProvider.NAVER
        MapProviderLocalEntity.GOOGLE -> MapProvider.GOOGLE
    }
