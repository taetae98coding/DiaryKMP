package io.github.taetae98coding.diary.feature.web.ui.detail

// 선택 목록에 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val webDetailViewModeList: List<WebDetailViewMode> =
    listOf(
        WebDetailViewMode.URL,
        WebDetailViewMode.RESPONSE,
    )
