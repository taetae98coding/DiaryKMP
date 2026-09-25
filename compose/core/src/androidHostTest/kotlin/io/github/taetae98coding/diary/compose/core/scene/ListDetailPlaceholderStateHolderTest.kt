@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 상세 placeholder는 목록과 상세를 함께 표시할 때만 그려지므로 두 영역을 함께 표시하는 너비에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h800dp")
class ListDetailPlaceholderStateHolderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = mutableStateListOf<Any>(ListKey)
    private val viewModelList = mutableListOf<PlaceholderViewModel>()

    @Test
    fun `목록 entry가 back stack에 남아 있으면 단독 화면에 다녀와도 placeholder의 저장 상태와 ViewModel을 유지한다`() {
        val input = fixtureInput()
        composeRule.setContent { PlaceholderNavDisplay() }
        composeRule.onNode(hasSetTextAction()).performTextInput(input)

        composeRule.runOnIdle { backStack.add(FullScreenKey) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(FULL_SCREEN_CONTENT).assertExists()
        composeRule.onNode(hasSetTextAction()).assertDoesNotExist()

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(input))
        viewModelList.distinct() shouldHaveSize 1
        viewModelList.first().isCleared shouldBe false
    }

    @Test
    fun `목록 entry가 back stack에서 빠지면 placeholder의 저장 상태와 ViewModel을 비운다`() {
        val input = fixtureInput()
        composeRule.setContent { PlaceholderNavDisplay() }
        composeRule.onNode(hasSetTextAction()).performTextInput(input)
        val firstViewModel = viewModelList.single()

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(OtherKey)
        }
        composeRule.waitForIdle()
        firstViewModel.isCleared shouldBe true

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(ListKey)
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(""))
        viewModelList.distinct() shouldHaveSize 2
        viewModelList.last().isCleared shouldBe false
    }

    @Test
    fun `화면이 다시 만들어져 복원되어도 placeholder의 저장 상태를 유지한다`() {
        val input = fixtureInput()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceholderNavDisplay() }
        composeRule.onNode(hasSetTextAction()).performTextInput(input)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(input))
    }

    @Test
    fun `저장소가 제공되지 않으면 placeholder를 그대로 그린다`() {
        val viewModelStoreOwner = testViewModelStoreOwner()
        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ListDetailPlaceholderStateProvider(listContentKey = LIST_CONTENT_KEY) {
                    Placeholder()
                }
            }
        }

        composeRule.onNode(hasSetTextAction()).assertExists()
        viewModelList.single() shouldBeSameInstanceAs
            ViewModelProvider.create(owner = viewModelStoreOwner, factory = viewModelFactory { initializer { PlaceholderViewModel() } })[PlaceholderViewModel::class]
    }

    @Composable
    private fun PlaceholderNavDisplay() {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner = remember { testViewModelStoreOwner() }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            val holder = rememberListDetailPlaceholderStateHolder()

            CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                DiaryTheme {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<ListKey>(
                                    clazzContentKey = { LIST_CONTENT_KEY },
                                    metadata =
                                        ListDetailSceneStrategy.listPane(
                                            sceneKey = ListKey,
                                            detailPlaceholder = {
                                                ListDetailPlaceholderStateProvider(listContentKey = LIST_CONTENT_KEY) {
                                                    Placeholder()
                                                }
                                            },
                                        ),
                                ) { Text(text = LIST_CONTENT) }
                                entry<FullScreenKey> { Text(text = FULL_SCREEN_CONTENT) }
                                entry<OtherKey> { Text(text = OTHER_CONTENT) }
                            },
                    )
                }
            }
        }
    }

    @Composable
    private fun Placeholder() {
        val viewModel = viewModel { PlaceholderViewModel() }
        var text by rememberSaveable { mutableStateOf("") }

        remember(viewModel) { viewModelList.add(viewModel) }
        BasicTextField(value = text, onValueChange = { value -> text = value })
    }

    private class PlaceholderViewModel : ViewModel() {
        var isCleared: Boolean = false
            private set

        override fun onCleared() {
            isCleared = true
        }
    }

    private data object ListKey

    private data object FullScreenKey

    private data object OtherKey

    private companion object {
        const val LIST_CONTENT_KEY = "ListKey"
        const val LIST_CONTENT = "ListContent"
        const val FULL_SCREEN_CONTENT = "FullScreenContent"
        const val OTHER_CONTENT = "OtherContent"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun fixtureInput(): String = "input-${fixtureMonkey.giveMeOne<String>()}"

        fun testViewModelStoreOwner(): ViewModelStoreOwner =
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
    }
}
