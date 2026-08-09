package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.compose.map.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider

public fun MapProvider.toDiaryMapProvider(): DiaryMapProvider =
    when (this) {
        MapProvider.NAVER -> DiaryMapProvider.NAVER
        MapProvider.GOOGLE -> DiaryMapProvider.GOOGLE
    }

public fun DiaryMapProvider.toMapProvider(): MapProvider =
    when (this) {
        DiaryMapProvider.NAVER -> MapProvider.NAVER
        DiaryMapProvider.GOOGLE -> MapProvider.GOOGLE
    }
