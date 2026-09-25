@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 상세 영역의 루틴 추가는 목록과 상세를 함께 표시할 때만 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h800dp")
class RoutineListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(RoutineHomeNavKey)

    @Test
    fun `TC-ROUTINE-LIST-DETAIL-DOMAIN-003 다른 주요 목적지에 다녀오면 상세 영역의 루틴 추가는 제목과 설명이 비어 있는 상태로 시작한다`() {
        val title = "title-${fixtureMonkey.giveMeOne<String>()}"
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        setRoutineNavDisplay()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(title)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(title))

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(OtherTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(OTHER_TOP_LEVEL_CONTENT).onFirst().assertExists()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(RoutineHomeNavKey)
        }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(""))
    }

    private fun setRoutineNavDisplay() {
        composeRule.setContent {
            val holder = rememberListDetailPlaceholderStateHolder()

            CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                DiaryTheme {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<RoutineHomeNavKey>(
                                    clazzContentKey = { ROUTINE_HOME_CONTENT_KEY },
                                    metadata = routineHomeListPaneMetadata(),
                                ) { Text(text = ROUTINE_HOME_CONTENT) }
                                entry<OtherTopLevelNavKey> { Text(text = OTHER_TOP_LEVEL_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val ROUTINE_HOME_CONTENT = "RoutineHomeContent"
        const val OTHER_TOP_LEVEL_CONTENT = "OtherTopLevelContent"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

// 캘린더 홈처럼 루틴이 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}
