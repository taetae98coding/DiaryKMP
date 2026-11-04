package io.github.taetae98coding.diary.app.shared.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppDeepLinkNavigationTest :
    FunSpec({
        test("지원하는 각 딥링크 주소에 매치되는 navigation은 정확히 하나다") {
            listOf(
                ScreenDeepLink.FILE_HOME,
                "diary://timetable?type=day&date=2026-11-04",
                "diary://timetable?type=week&date=2026-11-01",
            ).forEach { deepLink ->
                val request = DeepLinkRequest(deepLink)

                val matchedCount =
                    appDeepLinkNavigationList.count { navigation ->
                        navigation.open(request = request, backStack = NavBackStack<ScreenNavKey>(TopLevelNavigation.DEFAULT.key))
                    }

                matchedCount shouldBe 1
            }
        }
    })
