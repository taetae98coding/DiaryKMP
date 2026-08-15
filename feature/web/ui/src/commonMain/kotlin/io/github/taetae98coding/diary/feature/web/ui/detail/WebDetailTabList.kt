package io.github.taetae98coding.diary.feature.web.ui.detail

// 탭과 영역이 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val webDetailTabList: List<WebDetailTab> =
    listOf(
        WebDetailTab.FORM,
        WebDetailTab.PAGE,
    )
