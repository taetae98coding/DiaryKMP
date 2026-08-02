package io.github.taetae98coding.diary.feature.tag.ui.detail

// 탭과 페이지가 놓이는 순서는 스펙이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val tagDetailTabList: List<TagDetailTab> =
    listOf(
        TagDetailTab.DETAIL,
        TagDetailTab.MEMO,
        TagDetailTab.WEB,
        TagDetailTab.PLACE,
    )
