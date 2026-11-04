package io.github.taetae98coding.diary.app.shared.navigation

import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.ui.fileHomeDeepLinkNavigation
import io.github.taetae98coding.diary.feature.timetable.ui.timetableDeepLinkNavigation

internal val appDeepLinkNavigationList: List<DeepLinkNavigation> =
    listOf(
        fileHomeDeepLinkNavigation(parentNavKeyList = fileHomeParentNavKeyList()),
        timetableDeepLinkNavigation(),
    )

// 목록이 먼저 초기화되므로 뒤에 선언한 프로퍼티 대신 함수로 부모 백스택을 만든다.
private fun fileHomeParentNavKeyList(): List<ScreenNavKey> =
    listOf(TopLevelNavigation.DEFAULT, TopLevelNavigation.More)
        .distinct()
        .map(TopLevelNavigation::key)
