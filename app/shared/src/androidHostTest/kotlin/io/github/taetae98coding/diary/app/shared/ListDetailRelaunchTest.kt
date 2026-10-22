package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1000dp-h800dp")
class ListDetailRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var launchCount by mutableIntStateOf(0)

    @Test
    fun `TC-4be50706-f80f-47c6-8870-aa3eb113bf05 앱을 종료하고 다시 실행하면 체크리스트 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailResetAfterRelaunch(
            homeNavKey = ChecklistHomeNavKey,
            detailNavKey = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-40190e97-24a5-48a0-bb95-e00a19c8c882 앱을 종료하고 다시 실행하면 파일 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailResetAfterRelaunch(
            homeNavKey = FileHomeNavKey,
            detailNavKey = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    private fun assertDetailResetAfterRelaunch(
        homeNavKey: ScreenNavKey,
        detailNavKey: ScreenNavKey,
    ) {
        val appState = relaunchableAppState()
        composeRule.runOnIdle {
            appState().navigateTo(TopLevelNavigation.More)
            appState().backStack.add(homeNavKey)
            appState().backStack.add(detailNavKey)
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
