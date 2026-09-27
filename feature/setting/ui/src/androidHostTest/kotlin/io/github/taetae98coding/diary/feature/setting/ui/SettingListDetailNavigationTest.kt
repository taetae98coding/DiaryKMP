@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.setting.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingBrowserNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.setting.api.settingNavKeys
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserUiState
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserViewModel
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadUiState
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadViewModel
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiUiState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiViewModel
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelUiState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelViewModel
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
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
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
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
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
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
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

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-SETTING-LIST-DETAIL-FEATURE-013 세부 설정이 단독으로 표시될 때 뒤로가면 설정 목록으로 돌아간다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertDoesNotExist()

        listOf(
            DEFAULT_HOLIDAY_ITEM_LABEL to SettingHolidayNavKey,
            DEFAULT_MAP_ITEM_LABEL to SettingMapNavKey,
            DEFAULT_GEMINI_ITEM_LABEL to SettingGeminiNavKey,
            DEFAULT_BROWSER_ITEM_LABEL to SettingBrowserNavKey,
            DEFAULT_DOWNLOAD_ITEM_LABEL to SettingDownloadNavKey,
        ).forEach { (itemLabel, detail) ->
            composeRule.onNodeWithText(itemLabel).performClick()
            composeRule.waitForIdle()
            backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, detail)
            composeRule.onNodeWithText(DEFAULT_SETTING_HOME_TITLE).assertDoesNotExist()

            pressBack()

            backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
            composeRule.onNodeWithText(DEFAULT_SETTING_HOME_TITLE).assertExists()
            settingItemLabelList.forEach { label -> composeRule.onNodeWithText(label).assertExists() }
            composeRule.onNodeWithText(MORE_CONTENT).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-014 화면이 재생성되어도 상세 선택을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restorableBackStack: NavBackStack<ScreenNavKey>
        restorationTester.setContent {
            val currentBackStack =
                rememberSerializable(
                    configuration = RestorationTestSavedStateConfiguration,
                    serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                ) {
                    NavBackStack<ScreenNavKey>(SettingHomeNavKey)
                }
            restorableBackStack = currentBackStack

            SettingNavigationTestHost {
                SettingNavDisplay(backStack = currentBackStack)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { restorableBackStack.toList() shouldBe listOf(SettingHomeNavKey, SettingMapNavKey) }
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).assertExists()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertDoesNotExist()

        pressBack()

        composeRule.runOnIdle { restorableBackStack.toList() shouldBe listOf(SettingHomeNavKey) }
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-015 앱이 백그라운드에서 돌아와도 상세 선택을 유지한다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, SettingMapNavKey)
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).assertExists()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertDoesNotExist()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setSettingNavDisplay() {
        composeRule.setContent {
            SettingNavigationTestHost {
                SettingNavDisplay(backStack = backStack)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_SETTING_HOME_TITLE).assertExists()
    }

    @Composable
    private fun SettingNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
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
            factory<SettingGeminiViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingGeminiUiState.Loading)
                    every { effect } returns emptyFlow()
                }
            }
            factory<SettingGeminiModelViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingGeminiModelUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory<SettingBrowserViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingBrowserUiState.Loading)
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
        const val DEFAULT_GEMINI_ITEM_LABEL = "Gemini"
        const val DEFAULT_BROWSER_ITEM_LABEL = "Browser"
        const val DEFAULT_DOWNLOAD_ITEM_LABEL = "Download"
        const val DEFAULT_SETTING_HOME_TITLE = "Settings"
        val settingItemLabelList: List<String> =
            listOf(DEFAULT_HOLIDAY_ITEM_LABEL, DEFAULT_MAP_ITEM_LABEL, DEFAULT_GEMINI_ITEM_LABEL, DEFAULT_BROWSER_ITEM_LABEL, DEFAULT_DOWNLOAD_ITEM_LABEL)
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

// 제품의 설정 화면 키 직렬화 등록을 그대로 써서 화면 재생성 때 전환 이력이 저장·복원되게 한다.
private val RestorationTestSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(ScreenNavKey::class) {
                    settingNavKeys()
                }
            }
    }
