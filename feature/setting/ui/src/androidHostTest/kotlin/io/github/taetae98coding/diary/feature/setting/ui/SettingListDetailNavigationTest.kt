@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.setting.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadUiState
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadViewModel
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayUiState
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayViewModel
import io.github.taetae98coding.diary.feature.setting.ui.home.SettingHomeUiState
import io.github.taetae98coding.diary.feature.setting.ui.home.SettingHomeViewModel
import io.github.taetae98coding.diary.feature.setting.ui.home.settingHomeItemList
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapUiState
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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

// 앱과 같은 목록·상세 전략과 제품 settingEntry로 두 영역을 함께 표시하는 너비의 NavDisplay를 구성해 시스템 뒤로가기 결과를 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class SettingListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(NavigationTestMoreNavKey, SettingHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-010 상세를 선택한 뒤 시스템 뒤로가기를 사용하면 선택 전 상태로 돌아간다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).assertExists()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-010 다른 상세로 바꾼 뒤 시스템 뒤로가기를 사용하면 이전 상세가 아닌 선택 전 상태로 돌아간다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_HOLIDAY_LOADING_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, SettingMapNavKey)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_HOLIDAY_LOADING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-012 선택 전 상태에서 시스템 뒤로가기를 사용하면 설정 화면 전체를 떠난다`() {
        setSettingNavDisplay()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-009 더보기로 돌아갔다가 다시 진입하면 상세 선택을 초기화한다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(SettingHomeNavKey) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-011 현재 상세인 설정 항목을 다시 선택해도 입력 중이던 내용이 남는다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_DOWNLOAD_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput(EDITING_PROXY_ADDRESS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DOWNLOAD_ITEM_LABEL).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, SettingDownloadNavKey)
        composeRule.onNode(hasSetTextAction()).assert(hasText(EDITING_PROXY_ADDRESS))
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setSettingNavDisplay() {
        composeRule.setContent {
            SettingNavigationTestHost {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<NavigationTestMoreNavKey> { Text(text = MORE_CONTENT) }
                            settingEntry(backStack = backStack)
                        },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
    }

    @Composable
    private fun SettingNavigationTestHost(content: @Composable () -> Unit) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(viewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 뒤로가기로 entry가 닫히면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun viewModelModule() =
        module {
            factory<SettingHomeViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingHomeUiState.Loaded(itemList = settingHomeItemList))
                }
            }
            factory<SettingHolidayViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingHolidayUiState.Loading)
                }
            }
            factory<SettingDownloadViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY))
                    every { effect } returns emptyFlow()
                }
            }
            factory<SettingMapViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER))
                }
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val DEFAULT_HOLIDAY_ITEM_LABEL = "Holiday"
        const val DEFAULT_MAP_ITEM_LABEL = "Map"
        const val DEFAULT_GOOGLE_LABEL = "Google"
        const val DEFAULT_DOWNLOAD_ITEM_LABEL = "Download"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val EDITING_PROXY_ADDRESS = "http://192.168.0.10:54321"
        const val DEFAULT_HOLIDAY_LOADING_DESCRIPTION = "Loading holidays"
    }
}

// 설정 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈 androidHostTest의 의존이 아니므로 대역을 사용한다.
private data object NavigationTestMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
