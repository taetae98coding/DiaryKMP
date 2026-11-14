@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.kotest.matchers.shouldBe
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

// 제품 routineEntry의 배치 정보를 그대로 쓰도록, 창 너비만 바꿔 가며 제품 entry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h800dp")
class RoutineListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(NavigationTestTopLevelNavKey, RoutineHomeNavKey)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-8c88c858-2072-400f-a879-00952bb3e90e 목록과 상세를 함께 쓰는 환경에서는 루틴 목록과 루틴 추가를 함께 표시한다`() {
        setRoutineNavDisplay()

        composeRule.onNodeWithText(DEFAULT_HOME_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        backStack.toList() shouldBe listOf(NavigationTestTopLevelNavKey, RoutineHomeNavKey)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-8beb00fb-4162-4842-b012-03becf084f00 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        setRoutineNavDisplay()

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe RoutineAddNavKey()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-f731c7b3-d20c-4718-ae02-b9287ed7d17a 단독으로 표시된 루틴 추가에서 뒤로가면 RoutineHome으로 돌아간다`() {
        setRoutineNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestTopLevelNavKey, RoutineHomeNavKey)
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-8ac57d1f-2a84-4caa-8944-bbebde25bf75 상세 영역에 루틴 추가가 놓인 상태에서 뒤로가면 배치를 떠나 기본 목적지로 돌아간다`() {
        setRoutineNavDisplay()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestTopLevelNavKey)
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
    }

    private fun setRoutineNavDisplay() {
        val viewModelModule =
            module {
                factory { routineHomeViewModel() }
                factory { routineHomeCalendarViewModel() }
                factory { routineHomeHolidayViewModel() }
                factory { routineSyncRefreshViewModel() }
                factory { routineAddViewModel() }
                factory { routineAddTagViewModel() }
            }

        composeRule.setContent {
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }

            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    RoutineTestTheme {
                        val holder = rememberListDetailPlaceholderStateHolder()

                        CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
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
                                        entry<NavigationTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        routineEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DEFAULT_HOME_TITLE = "Routine"
        const val DEFAULT_ADD_TITLE = "Add Routine"
        const val DEFAULT_EMPTY_TITLE = "No routines yet"
        const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}

// 기본 목적지인 캘린더 홈처럼 루틴이 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object NavigationTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "NavigationTestTopLevel"
}
