package io.github.taetae98coding.diary.app.shared

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RememberAppStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-002 앱을 캘린더 목적지에서 시작한다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Calendar
            appState.isNavigationVisible.shouldBeTrue()
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-007 넓은 창에서 세부 화면과 함께 표시되면 내비게이션을 유지한다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.add(MemoAddNavKey())
        }

        composeRule.runOnIdle {
            appState.isNavigationVisible.shouldBeTrue()
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-004 좁은 창에서 세부 화면으로 이동하면 내비게이션을 숨긴다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.add(MemoAddNavKey())
        }

        composeRule.runOnIdle {
            appState.isNavigationVisible.shouldBeFalse()
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-009 화면 재생성 후 목적지와 전환 이력을 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.More)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                )
            appState.backStack.removeLast()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }
    }

    @Test
    fun `요청 키를 가진 TagAdd 내비게이션 키를 화면 재생성 후 복원한다`() {
        val navKey = TagAddNavKey(requestKey = Uuid.random())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.add(navKey)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    navKey,
                )
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-017 목적지 위에 열려 있던 TagAdd 화면을 화면 재생성 후 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.add(TagAddNavKey())
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    TagAddNavKey(),
                )
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            appState.isNavigationVisible.shouldBeFalse()
        }
    }
}
