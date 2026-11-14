package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1000dp-h800dp")
class TagRoutineFinishedListRelaunchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-78a94fea-9043-47eb-84f3-a46fcdd90325 화면이 재생성되어도 완료된 루틴 목록과 함께 놓인 루틴 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = pathKeyList()
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.addAll(pathKeyList)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        assertDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    fun `TC-78a94fea-9043-47eb-84f3-a46fcdd90325 앱이 백그라운드에 다녀와도 완료된 루틴 목록과 함께 놓인 루틴 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = pathKeyList()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        lateinit var appState: AppState
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                appState = rememberAppState()
            }
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.addAll(pathKeyList)
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        assertDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 앱 상태를 다시 구성한다.
    @Test
    fun `TC-b07724bf-4c02-482d-b13e-bc58717f1cdb 앱을 종료하고 다시 실행하면 태그의 완료된 루틴 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        val pathKeyList = pathKeyList()
        var launchCount by mutableIntStateOf(0)
        lateinit var appState: AppState
        composeRule.setContent {
            key(launchCount) {
                appState = rememberAppState()
            }
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.addAll(pathKeyList)
        }
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key) + pathKeyList
        }

        composeRule.runOnIdle { launchCount += 1 }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
        }
    }

    private fun assertDetailKept(
        appState: AppState,
        pathKeyList: List<ScreenNavKey>,
    ) {
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key) + pathKeyList
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe pathKeyList[1]
        }
    }

    private fun pathKeyList(): List<ScreenNavKey> {
        val tagId = Arb.kotlinUuid().next()

        return listOf(TagDetailNavKey(id = tagId), TagRoutineFinishedListNavKey(tagId = tagId), RoutineDetailNavKey(id = Arb.kotlinUuid().next()))
    }
}
