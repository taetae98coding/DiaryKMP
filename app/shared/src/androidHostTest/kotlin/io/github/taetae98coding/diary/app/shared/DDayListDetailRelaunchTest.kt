package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeFilterNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1000dp-h800dp")
class DDayListDetailRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var launchCount by mutableIntStateOf(0)

    @Test
    fun `TC-2d3d35b3-3bcb-4950-b33a-4a2e148e5357 앱을 종료하고 다시 실행하면 디데이 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        val appState = relaunchableAppState()
        composeRule.runOnIdle {
            appState().navigateTo(TopLevelNavigation.More)
            appState().backStack.add(DDayHomeNavKey)
            appState().backStack.add(DDayDetailNavKey(id = Arb.kotlinUuid().next()))
        }

        relaunch()

        composeRule.runOnIdle {
            appState().backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
        }
    }

    @Test
    fun `TC-575a04bd-de93-4c2b-b5c4-890037374d35 필터를 연 채 앱을 다시 실행하면 디데이 화면에 진입해도 필터는 닫혀 있다`() {
        val appState = relaunchableAppState()
        composeRule.runOnIdle {
            appState().navigateTo(TopLevelNavigation.More)
            appState().backStack.add(DDayHomeNavKey)
            appState().backStack.add(DDayHomeFilterNavKey)
        }

        relaunch()

        composeRule.runOnIdle {
            appState().backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
            appState().navigateTo(TopLevelNavigation.More)
            appState().backStack.add(DDayHomeNavKey)
        }
        composeRule.runOnIdle {
            appState().backStack shouldNotContain DDayHomeFilterNavKey
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
