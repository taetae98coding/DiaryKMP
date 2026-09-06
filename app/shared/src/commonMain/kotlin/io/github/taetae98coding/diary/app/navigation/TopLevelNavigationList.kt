package io.github.taetae98coding.diary.app.navigation

// 공통 내비게이션에 놓이는 순서는 스펙이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val topLevelNavigationList: List<TopLevelNavigation> =
    listOf(
        TopLevelNavigation.Memo,
        TopLevelNavigation.Tag,
        TopLevelNavigation.Calendar,
        TopLevelNavigation.Routine,
        TopLevelNavigation.More,
    )
