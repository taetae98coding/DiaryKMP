package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk

class AppDeepLinkNavigationTest :
    FunSpec({
        test("지원하는 각 딥링크 주소에 매치되는 navigation은 정확히 하나다") {
            listOf(
                ScreenDeepLink.FILE_HOME,
                "diary://timetable?type=day&date=2026-11-04",
                "diary://timetable?type=week&date=2026-11-01",
                "diary://memo-detail?id=${Arb.kotlinUuid().next()}",
                "diary://tag-detail?id=${Arb.kotlinUuid().next()}",
                "diary://place-detail?id=${Arb.kotlinUuid().next()}",
                "diary://web-detail?id=${Arb.kotlinUuid().next()}",
                "diary://contact-detail?id=${Arb.kotlinUuid().next()}",
                "diary://checklist-detail?id=${Arb.kotlinUuid().next()}",
                "diary://qr-detail?id=${Arb.kotlinUuid().next()}",
                "diary://music-detail?id=${Arb.kotlinUuid().next()}",
                "diary://dday-detail?id=${Arb.kotlinUuid().next()}",
                "diary://web-add?initialTitle=a&initialUrl=https%3A%2F%2Fexample.com",
                "diary://memo-add?initialTitle=a&initialStartDate=2026-11-20&initialEndDate=2026-11-21",
                "diary://contact-add?initialName=a&initialPhoneNumberList=010&initialPhoneNumberList=02&initialBirthday=1990-03-15",
                "diary://routine-detail?id=${Arb.kotlinUuid().next()}",
            ).forEach { deepLink ->
                val request = DeepLinkRequest(deepLink)

                val matchedCount =
                    appDeepLinkNavigationList.count { navigation ->
                        navigation.open(request = request, navigator = createNavigator(), isLaunch = false)
                    }

                matchedCount shouldBe 1
            }
        }
    })

private fun createNavigator(): AppState =
    AppState(
        backStack = NavBackStack<ScreenNavKey>(TopLevelNavigation.DEFAULT.key),
        scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
        reselectEvent = TopLevelReselectEvent(),
        paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
    )
