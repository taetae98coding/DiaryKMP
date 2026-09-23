package io.github.taetae98coding.diary.feature.place.ui.home.viewmode

// 지도 모드로 시작하는 순서는 스펙이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val placeHomeViewModeList: List<PlaceHomeViewMode> =
    listOf(
        PlaceHomeViewMode.MAP,
        PlaceHomeViewMode.LIST,
    )
