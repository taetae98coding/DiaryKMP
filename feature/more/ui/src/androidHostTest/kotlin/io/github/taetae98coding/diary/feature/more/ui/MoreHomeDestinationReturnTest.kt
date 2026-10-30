package io.github.taetae98coding.diary.feature.more.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountUiState
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountViewModel
import io.github.taetae98coding.diary.feature.more.ui.home.menu.moreHomeMenuList
import io.github.taetae98coding.diary.feature.more.ui.home.refresh.MoreHomeRefreshViewModel
import io.github.taetae98coding.diary.feature.more.ui.home.signout.MoreHomeSignOutUiState
import io.github.taetae98coding.diary.feature.more.ui.home.signout.MoreHomeSignOutViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 공통 내비게이션이 목적지를 옮길 때처럼 전환 이력에서 더보기 목적지를 뺐다가 다시 넣고, 제품 moreEntry가 그 목적지를 처음 들어온 것처럼 시작하는지 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w240dp-h320dp")
class MoreHomeDestinationReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(DestinationTestTopLevelNavKey, MoreHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-875e0a2f-30d2-40fc-9359-65925970d3f9 더보기 목적지에 다녀오면 화면의 맨 위부터 시작한다`() {
        setMoreNavDisplay()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_LABEL).assertIsDisplayed()
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(moreHomeMenuList.size)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(DEFAULT_SIGN_IN_LABEL).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(DestinationTestTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(MoreHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SIGN_IN_LABEL).assertIsDisplayed()
    }

    // 디데이 바로가기로 들어간 화면은 이 모듈의 의존이 아니므로 대역으로 두고, 그 화면의 뒤로가기가 전환 이력에서 자신을 빼는 것은 TC-9441c865-0db9-40eb-901f-445b04bcad44의 테스트가 확인한다.
    @Test
    fun `TC-f4dab787-8259-4eb3-8ddc-f89c6fddbf0d 뒤로가기로 돌아간 더보기 화면은 떠나기 전에 보던 자리를 유지한다`() {
        setMoreNavDisplay()
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(moreHomeMenuList.size)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(DEFAULT_SIGN_IN_LABEL).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.runOnIdle { backStack.add(DestinationTestDetailNavKey) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(DestinationTestTopLevelNavKey, MoreHomeNavKey)
        composeRule.onAllNodesWithText(DEFAULT_SIGN_IN_LABEL).fetchSemanticsNodes().isEmpty() shouldBe true
    }

    private fun setMoreNavDisplay() {
        val viewModelModule =
            module {
                factory<MoreHomeAccountViewModel> {
                    mockk<MoreHomeAccountViewModel>(relaxed = true) {
                        every { uiState } returns MutableStateFlow(MoreHomeAccountUiState.Guest)
                    }
                }
                factory<MoreHomeSignOutViewModel> {
                    mockk<MoreHomeSignOutViewModel>(relaxed = true) {
                        every { uiState } returns MutableStateFlow(MoreHomeSignOutUiState())
                    }
                }
                factory<MoreHomeRefreshViewModel> { mockk<MoreHomeRefreshViewModel>(relaxed = true) }
            }

        composeRule.setContent {
            // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }

            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    DiaryTheme {
                        NavDisplay(
                            backStack = backStack,
                            // 보던 자리는 화면별 저장 상태가 정하므로, 앱이 쓰는 저장 상태 decorator만 둔다.
                            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                            entryProvider =
                                entryProvider {
                                    entry<DestinationTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                    entry<DestinationTestDetailNavKey> { Text(text = DETAIL_CONTENT) }
                                    moreEntry(backStack = backStack)
                                },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DETAIL_CONTENT = "DetailContent"
        const val DEFAULT_SIGN_IN_LABEL = "Sign in"
    }
}

// 캘린더 홈처럼 더보기가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object DestinationTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "DestinationTestTopLevel"
}

// `더보기`에서 이어지는 세부 화면을 대신한다.
private data object DestinationTestDetailNavKey : ScreenNavKey {
    override val screenName: String
        get() = "DestinationTestDetail"
}
