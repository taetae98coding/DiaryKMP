package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk

class TagRoutineNavigationTest :
    FunSpec({
        test("TagRoutineFinishedList ScreenNavKey를 저장하고 복원하면 tagId가 유지된다") {
            val key: ScreenNavKey = TagRoutineFinishedListNavKey(tagId = Arb.kotlinUuid().next())

            val savedState =
                encodeToSavedState(
                    value = key,
                    configuration = AppNavKeySavedStateConfiguration,
                )

            decodeFromSavedState<ScreenNavKey>(
                savedState = savedState,
                configuration = AppNavKeySavedStateConfiguration,
            ) shouldBe key
        }

        test("TC-50de687e-9a52-4974-99c6-b56bf45bee00 넓은 화면에서도 완료된 루틴 목록과 그 상세 영역의 루틴 상세에서는 공통 내비게이션을 숨긴다") {
            val tagId = Arb.kotlinUuid().next()
            val finishedListBackStack =
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    TagDetailNavKey(id = tagId),
                    TagRoutineFinishedListNavKey(tagId = tagId),
                )
            val backStackCases =
                listOf(
                    finishedListBackStack,
                    finishedListBackStack + RoutineDetailNavKey(id = Arb.kotlinUuid().next()),
                )

            backStackCases.forEach { keys ->
                val appState =
                    AppState(
                        backStack = NavBackStack(*keys.toTypedArray()),
                        scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                        reselectEvent = TopLevelReselectEvent(),
                        paneScaffoldDirectiveProvider = {
                            PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
                        },
                    )

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            }
        }
    })
