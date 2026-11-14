package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeFilterRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var launchCount by mutableIntStateOf(0)

    @Test
    fun `TC-17ceb6ac-a4b0-4e0f-b315-6ff45b4645c4 필터를 연 채 앱을 다시 실행하면 루틴 화면에 진입해도 필터는 닫혀 있다`() {
        val appState = relaunchableAppState()
        composeRule.runOnIdle {
            appState().navigateTo(TopLevelNavigation.Routine)
            appState().backStack.add(RoutineHomeFilterNavKey)
            appState().backStack.last() shouldBe RoutineHomeFilterNavKey
        }

        relaunch()

        composeRule.runOnIdle {
            appState().backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
            appState().navigateTo(TopLevelNavigation.Routine)
        }
        composeRule.runOnIdle {
            appState().backStack.last() shouldBe RoutineHomeNavKey
            appState().backStack shouldNotContain RoutineHomeFilterNavKey
        }
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 앱 상태를 다시 구성한다.
    private fun relaunchableAppState(): () -> AppState {
        lateinit var appState: AppState
        composeRule.setContent {
            key(launchCount) {
                appState = rememberAppState()
            }
        }
        composeRule.waitForIdle()

        return { appState }
    }

    private fun relaunch() {
        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()
    }
}
