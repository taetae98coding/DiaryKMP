package io.github.taetae98coding.diary.feature.setting.ui.map

import io.github.taetae98coding.diary.core.model.map.MapProvider

// 기본 지도 줄이 놓이는 순서는 스펙이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val settingMapProviderList: List<MapProvider> =
    listOf(
        MapProvider.NAVER,
        MapProvider.GOOGLE,
    )
