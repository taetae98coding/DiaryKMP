package io.github.taetae98coding.diary.compose.map

// 제공자 전환 항목이 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val diaryMapProviderList: List<DiaryMapProvider> =
    listOf(
        DiaryMapProvider.NAVER,
        DiaryMapProvider.GOOGLE,
    )
