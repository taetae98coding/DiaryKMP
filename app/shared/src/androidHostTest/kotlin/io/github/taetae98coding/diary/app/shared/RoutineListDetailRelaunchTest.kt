package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1000dp-h800dp")
class RoutineListDetailRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var launchCount by mutableIntStateOf(0)

    @Test
    fun `TC-133d16f0-0aee-4d09-8383-e510d00ac379 앱을 종료하고 다시 실행하면 루틴 상세 선택 없이 시작 화면에서 시작한다`() {
        val appState = relaunchableAppState()
        val detailKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next())
        composeRule.runOnIdle {
            appState().navigateTo(TopLevelNavigation.Routine)
            appState().backStack.add(detailKey)
        }
        composeRule.runOnIdle {
            appState().backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, RoutineHomeNavKey, detailKey)
        }

        relaunch()

        composeRule.runOnIdle {
            appState().backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
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
