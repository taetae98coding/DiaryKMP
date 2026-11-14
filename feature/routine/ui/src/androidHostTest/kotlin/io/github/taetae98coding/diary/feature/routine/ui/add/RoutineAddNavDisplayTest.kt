package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTitleInput
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val addViewModelList = mutableListOf<RoutineAddViewModel>()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-57168964-d153-4da0-8dd7-6d2f1bac0db7 TagDetail 루틴 탭에서 진입한 화면에서 뒤로가면 루틴 탭이 선택된 같은 태그의 TagDetail로 돌아간다`() {
        val tagDetailKey = TagDetailNavKey(id = Arb.kotlinUuid().next())
        val backStack = NavBackStack<ScreenNavKey>(RoutineAddNavDisplayMoreNavKey, tagDetailKey)
        setNavDisplay(backStack)
        // TagDetail의 루틴 탭을 고른 뒤 그 탭의 추가로 RoutineAdd에 진입한다.
        composeRule.onNodeWithText(TAG_DETAIL_ROUTINE_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(RoutineAddNavKey(initialTagId = tagDetailKey.id)) }
        composeRule.waitForIdle()
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)

        composeRule.onNodeWithContentDescription(ROUTINE_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineAddNavDisplayMoreNavKey, tagDetailKey)
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_ROUTINE_TAB").assertIsDisplayed()
        addViewModelList.size shouldBe 1
        addViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(input = any(), tagIdSet = any()) } }
    }

    private fun setNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<RoutineAddNavDisplayMoreNavKey> { Text(text = MORE_CONTENT) }
                            entry<TagDetailNavKey> { TagDetailStandIn() }
                            routineEntry(
                                backStack = backStack,
                                homeReselectEvent = emptyFlow(),
                            )
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    // TagDetail 화면을 대신한다. 탭 선택은 그 화면이 저장하는 상태이므로 같은 방식으로 저장해 돌아왔을 때의 탭을 드러낸다.
    @Composable
    private fun TagDetailStandIn() {
        var selectedTab by rememberSaveable { mutableStateOf(TAG_DETAIL_MEMO_TAB) }

        Column {
            Text(text = "$TAG_DETAIL_SELECTED_TAB_PREFIX$selectedTab")
            TextButton(onClick = { selectedTab = TAG_DETAIL_ROUTINE_TAB }) { Text(text = TAG_DETAIL_ROUTINE_TAB) }
        }
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                RoutineTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory { routineFormAddTagViewModel() }
            factory {
                mockk<RoutineAddViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(RoutineAddUiState())
                        every { effect } returns emptyFlow()
                    }.also(addViewModelList::add)
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val DRINK_WATER = "물 마시기"
        const val TAG_DETAIL_MEMO_TAB = "MemoTab"
        const val TAG_DETAIL_ROUTINE_TAB = "RoutineTab"
        const val TAG_DETAIL_SELECTED_TAB_PREFIX = "Selected: "
    }
}

// 루틴 화면으로 들어가는 앞선 화면을 대신한다. 그 화면의 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object RoutineAddNavDisplayMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
