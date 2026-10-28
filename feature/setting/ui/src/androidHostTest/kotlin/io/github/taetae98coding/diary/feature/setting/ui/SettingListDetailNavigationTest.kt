@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.setting.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
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
import io.github.taetae98coding.diary.feature.setting.api.SettingFileNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingPlaylistNavKey
import io.github.taetae98coding.diary.feature.setting.api.settingNavKeys
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserUiState
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserViewModel
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileUiState
import io.github.taetae98coding.diary.feature.setting.ui.file.SettingFileViewModel
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
import io.github.taetae98coding.diary.feature.setting.ui.playlist.SettingPlaylistUiState
import io.github.taetae98coding.diary.feature.setting.ui.playlist.SettingPlaylistViewModel
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
    fun `TC-7991766b-0000-4738-aa2d-27ff26d36bd5 상세를 선택한 뒤 시스템 뒤로가기를 사용하면 선택 전 상태로 돌아간다`() {
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
    fun `TC-7991766b-0000-4738-aa2d-27ff26d36bd5 다른 상세로 바꾼 뒤 시스템 뒤로가기를 사용하면 이전 상세가 아닌 선택 전 상태로 돌아간다`() {
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
    fun `TC-3a4ccfbf-2c11-4219-9cb6-66157dd4164a 선택 전 상태에서 시스템 뒤로가기를 사용하면 설정 화면 전체를 떠난다`() {
        setSettingNavDisplay()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-70fdb7cb-f9cb-4fb6-b787-e33c9e87c1fc 선택 전 상태의 설정 목록에는 설정 항목과 뒤로가기만 있고 Cmd A 단축키로 새 화면이 열리지 않는다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()

        composeRule.onAllNodes(hasClickAction()).assertCountEquals(settingItemLabelList.size + 1)
        settingItemLabelList.forEach { label -> composeRule.onNodeWithText(label).assert(hasClickAction()) }
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
        composeRule.onAllNodes(hasContentDescription(ADD_DESCRIPTION_WORD, substring = true)).assertCountEquals(0)
        composeRule.onAllNodesWithText(ADD_DESCRIPTION_WORD, substring = true).assertCountEquals(0)

        pressAddShortcut()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey)
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-70fdb7cb-f9cb-4fb6-b787-e33c9e87c1fc 지도 설정이 현재 상세일 때 설정 목록에 추가 동작이 없고 Cmd A 단축키로 새 화면이 열리지 않는다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_MAP_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()

        composeRule.onAllNodes(hasContentDescription(ADD_DESCRIPTION_WORD, substring = true)).assertCountEquals(0)
        composeRule.onAllNodesWithText(ADD_DESCRIPTION_WORD, substring = true).assertCountEquals(0)

        pressAddShortcut()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, SettingMapNavKey)
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()
        settingItemLabelList.forEach { label -> composeRule.onNodeWithText(label).assertExists() }
    }

    @Test
    fun `TC-8bf5ef36-7ebb-40fc-8f89-e6d04aecc8c1 더보기로 돌아갔다가 다시 진입하면 상세 선택을 초기화한다`() {
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
    fun `TC-d460b908-7f6d-45ff-87be-813e714c539b 현재 상세인 설정 항목을 다시 선택해도 입력 중이던 내용이 남는다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithText(DEFAULT_PLAYLIST_ITEM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput(EDITING_PROXY_ADDRESS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PLAYLIST_ITEM_LABEL).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, SettingHomeNavKey, SettingPlaylistNavKey)
        composeRule.onNode(hasSetTextAction()).assert(hasText(EDITING_PROXY_ADDRESS))
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-8cbf380d-d0b9-456a-a873-f94e35d5deea 세부 설정이 단독으로 표시될 때 뒤로가면 설정 목록으로 돌아간다`() {
        setSettingNavDisplay()
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertDoesNotExist()

        listOf(
            DEFAULT_HOLIDAY_ITEM_LABEL to SettingHolidayNavKey,
            DEFAULT_MAP_ITEM_LABEL to SettingMapNavKey,
            DEFAULT_GEMINI_ITEM_LABEL to SettingGeminiNavKey,
            DEFAULT_BROWSER_ITEM_LABEL to SettingBrowserNavKey,
            DEFAULT_PLAYLIST_ITEM_LABEL to SettingPlaylistNavKey,
            DEFAULT_FILE_ITEM_LABEL to SettingFileNavKey,
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
    fun `TC-7d9c2fbb-2838-4431-903e-e94079265930 화면이 재생성되어도 상세 선택을 유지한다`() {
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
    fun `TC-ad29bf0e-2beb-4725-952f-9b6b54ca1352 시스템이 앱을 정리했다가 다시 만들어도 상세 선택과 뒤로가기 기록을 유지한다`() {
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
        // 앞서 고른 상세가 교체되어 뒤로가기 기록에 남지 않은 채로 되살아나는지 보려고 다른 항목을 먼저 고른다.
        composeRule.onNodeWithText(DEFAULT_HOLIDAY_ITEM_LABEL).performClick()
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
    fun `TC-152b5755-8409-43bd-95bf-0db8426a0a60 앱이 백그라운드에서 돌아와도 상세 선택을 유지한다`() {
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

    private fun pressAddShortcut() {
        // 화면 위에 작은 팝업 창이 함께 떠 있을 수 있어, 앱 창의 루트로 단축키를 보낸다.
        composeRule.onAllNodes(isRoot()).onFirst().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()
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
                    every { uiState } returns MutableStateFlow(SettingHomeUiState.Content(itemList = settingHomeItemList))
                }
            }
            factory<SettingHolidayViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingHolidayUiState.Loading)
                }
            }
            factory<SettingPlaylistViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY))
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
                }
            }
            factory<SettingBrowserViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingBrowserUiState.Loading)
                }
            }
            factory<SettingFileViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingFileUiState.Content(directory = null))
                    every { effect } returns emptyFlow()
                }
            }
            factory<SettingMapViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SettingMapUiState.Content(defaultProvider = MapProvider.NAVER))
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
        const val DEFAULT_PLAYLIST_ITEM_LABEL = "Playlist"
        const val DEFAULT_FILE_ITEM_LABEL = "Files"
        const val DEFAULT_SETTING_HOME_TITLE = "Settings"
        val settingItemLabelList: List<String> =
            listOf(DEFAULT_HOLIDAY_ITEM_LABEL, DEFAULT_MAP_ITEM_LABEL, DEFAULT_GEMINI_ITEM_LABEL, DEFAULT_BROWSER_ITEM_LABEL, DEFAULT_PLAYLIST_ITEM_LABEL, DEFAULT_FILE_ITEM_LABEL)
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val EDITING_PROXY_ADDRESS = "http://192.168.0.10:54321"
        const val DEFAULT_HOLIDAY_LOADING_DESCRIPTION = "Loading holidays"
        const val ADD_DESCRIPTION_WORD = "Add"
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
