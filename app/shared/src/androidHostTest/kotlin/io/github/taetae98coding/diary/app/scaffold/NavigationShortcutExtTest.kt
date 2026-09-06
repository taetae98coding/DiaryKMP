package io.github.taetae98coding.diary.app.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.app.AppState
import io.github.taetae98coding.diary.app.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationShortcutExtTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `단축키로 주요 목적지를 전환한다`() {
        val appState = createShortcutAppState(TopLevelNavigation.DEFAULT.key)
        setShortcutContent(appState)

        shortcutCases.forEach { case ->
            performShortcut(case.key)

            composeRule.runOnIdle {
                appState.currentTopLevelNavigation shouldBe case.destination
            }
        }
    }

    @Test
    fun `세부 화면에서는 단축키를 처리하지 않는다`() {
        val appState =
            createShortcutAppState(
                TopLevelNavigation.DEFAULT.key,
                TopLevelNavigation.More.key,
                LoginHomeNavKey,
            )
        val initialBackStack = appState.backStack.toList()
        setShortcutContent(appState)

        shortcutCases.forEach { case -> performShortcut(case.key) }

        composeRule.runOnIdle {
            appState.backStack.toList() shouldBe initialBackStack
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
        }
    }

    private fun setShortcutContent(appState: AppState) {
        composeRule.setContent {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .semantics { testTag = SHORTCUT_TEST_TAG }
                        .navigationShortcut(appState),
            )
        }
        composeRule.waitForIdle()
    }

    private fun performShortcut(key: Key) {
        composeRule.onNodeWithTag(SHORTCUT_TEST_TAG).performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(key)
            keyUp(key)
            keyUp(Key.MetaLeft)
        }
    }

    public companion object {
        private const val SHORTCUT_TEST_TAG = "navigationShortcut"

        private val shortcutCases =
            listOf(
                ShortcutCase(key = Key.One, destination = TopLevelNavigation.Memo),
                ShortcutCase(key = Key.Two, destination = TopLevelNavigation.Tag),
                ShortcutCase(key = Key.Three, destination = TopLevelNavigation.Calendar),
                ShortcutCase(key = Key.Four, destination = TopLevelNavigation.Routine),
                ShortcutCase(key = Key.Five, destination = TopLevelNavigation.More),
            )

        private fun createShortcutAppState(vararg keys: NavKey): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
            )
    }
}

private data class ShortcutCase(
    val key: Key,
    val destination: TopLevelNavigation,
)
