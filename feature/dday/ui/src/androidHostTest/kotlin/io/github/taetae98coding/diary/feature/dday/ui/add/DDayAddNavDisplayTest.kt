package io.github.taetae98coding.diary.feature.dday.ui.add

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
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.ui.dDayEntry
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayAddRepeatOf
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayFormAddTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeViewModel
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
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
class DDayAddNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val addViewModelList = mutableListOf<DDayAddViewModel>()

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트 앞뒤로 전역 Koin을 정리한다.
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
    fun `TC-a5b979e2-1447-4846-a7c0-c86d3e0a12d6 DDayHome에서 진입한 화면에서 뒤로가면 DDayHome으로 돌아가고 아무것도 저장하지 않는다`() {
        val backStack = NavBackStack<ScreenNavKey>(DDayAddNavDisplayMoreNavKey, DDayHomeNavKey, DDayAddNavKey())
        setNavDisplay(backStack)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayAddNavDisplayMoreNavKey, DDayHomeNavKey)
        composeRule.onNodeWithText(DDAY_HOME_TITLE).assertIsDisplayed()
        addViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(input = any(), tagIdSet = any()) } }
    }

    @Test
    fun `TC-8d46f02a-5b58-4d36-bbdd-a8e99bca19f7 TagDetail 디데이 탭에서 진입한 화면에서 뒤로가면 디데이 탭이 선택된 같은 태그의 TagDetail로 돌아간다`() {
        val tagDetailKey = TagDetailNavKey(id = Arb.kotlinUuid().next())
        val backStack = NavBackStack<ScreenNavKey>(DDayAddNavDisplayMoreNavKey, tagDetailKey)
        setNavDisplay(backStack)
        // TagDetail의 디데이 탭을 고른 뒤 그 탭의 추가로 DDayAdd에 진입한다.
        composeRule.onNodeWithText(TAG_DETAIL_DDAY_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(DDayAddNavKey(initialTagId = tagDetailKey.id)) }
        composeRule.waitForIdle()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DDayAddNavDisplayMoreNavKey, tagDetailKey)
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_DDAY_TAB").assertIsDisplayed()
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
                            entry<DDayAddNavDisplayMoreNavKey> { Text(text = MORE_CONTENT) }
                            entry<TagDetailNavKey> { TagDetailStandIn() }
                            dDayEntry(backStack = backStack)
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
            TextButton(onClick = { selectedTab = TAG_DETAIL_DDAY_TAB }) { Text(text = TAG_DETAIL_DDAY_TAB) }
        }
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                DDayFormTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory { ddayFormAddTagViewModel() }
            factory {
                mockk<DDayHomeViewModel>(relaxed = true).apply {
                    every { ddayPagingData } returns MutableStateFlow(PagingData.empty())
                    every { filterUiState } returns MutableStateFlow(DDayHomeScaffoldFilterUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory {
                mockk<DDayAddViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(DDayAddUiState())
                        every { effect } returns emptyFlow()
                    }.also(addViewModelList::add)
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val DDAY_HOME_TITLE = "D-Day"
        const val MARRIAGE = "결혼"
        const val TAG_DETAIL_MEMO_TAB = "MemoTab"
        const val TAG_DETAIL_DDAY_TAB = "DDayTab"
        const val TAG_DETAIL_SELECTED_TAB_PREFIX = "Selected: "
    }
}

// 디데이 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object DDayAddNavDisplayMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
