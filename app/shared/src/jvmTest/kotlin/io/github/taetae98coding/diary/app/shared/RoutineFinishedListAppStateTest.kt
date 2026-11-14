package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.openDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk

class RoutineFinishedListAppStateTest :
    FunSpec({
        test("TC-3201659a-877a-4d07-8fb1-38530849b0ad 완료된 루틴 목록에서는 화면 너비와 관계없이 내비게이션 숨김") {
            listOf(false, true).forEach { isListDetailTwoPane ->
                val appState =
                    createAppState(
                        *(routineBackStack() + RoutineFinishedListNavKey).toTypedArray(),
                        isListDetailTwoPane = isListDetailTwoPane,
                    )

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Routine
            }
        }

        test("TC-1c6606cb-c095-4dc9-b0ca-e967b5721ef1 넓은 화면에서 완료된 루틴 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            listOf(null, RoutineDetailNavKey(id = Arb.kotlinUuid().next())).forEach { detailKey ->
                val backStack = routineBackStack() + listOfNotNull(RoutineFinishedListNavKey, detailKey)
                val appState = createAppState(*backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Routine
            }
        }

        test("TC-3a869372-c887-493c-b5e1-e599dfc5c18d 파일 올리기 알림으로 FileHome이 열린 뒤 루틴으로 돌아오면 완료된 루틴 목록과 상세가 남지 않는다") {
            val backStack = routineBackStack() + listOf(RoutineFinishedListNavKey, RoutineDetailNavKey(id = Arb.kotlinUuid().next()))
            val appState = createAppState(*backStack.toTypedArray(), isListDetailTwoPane = true)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
            appState.navigateTo(TopLevelNavigation.Routine)

            appState.backStack.toList() shouldBe routineBackStack()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Routine
        }
    }) {
    private companion object {
        private fun createAppState(
            vararg keys: ScreenNavKey,
            isListDetailTwoPane: Boolean = false,
        ): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = {
                    if (isListDetailTwoPane) {
                        PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
                    } else {
                        PaneScaffoldDirective.Default
                    }
                },
            )

        private fun routineBackStack(): List<ScreenNavKey> = listOf(TopLevelNavigation.DEFAULT, TopLevelNavigation.Routine).map(TopLevelNavigation::key)
    }
}
