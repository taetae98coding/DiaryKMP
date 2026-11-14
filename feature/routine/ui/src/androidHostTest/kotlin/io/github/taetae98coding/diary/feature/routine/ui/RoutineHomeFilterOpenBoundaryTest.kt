package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.routineNavKeys
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 필터는 루틴 목록 위에 쌓이는 별도 화면이라, 제품 routineEntry와 앱과 같은 장면 전략으로 루틴 목적지를 띄운 채 경계를 지난다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeFilterOpenBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)

    private lateinit var currentBackStack: NavBackStack<ScreenNavKey>

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-63948bb2-bf13-450a-ba48-60863d40a2f7 필터를 연 채 화면이 재생성되어도 필터는 열린 채로 남는다`() {
        val tag = filterTag()
        val viewModelStoreOwner = routineListDetailViewModelStoreOwner()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            RestorableContent { navBackStack -> environment.Content(navBackStack = navBackStack, viewModelStoreOwner = viewModelStoreOwner) }
        }
        openFilterAndSelect(tag = tag)
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-63948bb2-bf13-450a-ba48-60863d40a2f7 필터를 연 채 앱이 백그라운드에 다녀와도 필터는 열린 채로 남는다`() {
        val tag = filterTag()
        composeRule.setContent { RestorableContent { navBackStack -> environment.Content(navBackStack = navBackStack) } }
        openFilterAndSelect(tag = tag)
        assertFilterOpened(tag = tag)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-63948bb2-bf13-450a-ba48-60863d40a2f7 필터를 연 채 시스템이 앱을 정리했다 다시 만들어도 필터는 열린 채로 남는다`() {
        val tag = filterTag()
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 ViewModel도 사라지므로, 다시 만들 때 새 소유자에서 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            RestorableContent { navBackStack ->
                environment.Content(navBackStack = navBackStack, viewModelStoreOwner = remember { routineListDetailViewModelStoreOwner() })
            }
        }
        openFilterAndSelect(tag = tag)
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
        environment.homeViewModelList shouldHaveAtLeastSize 2
    }

    // 전환 이력은 테스트가 들고 있는 객체가 아니라 저장 상태로 만들어, 경계를 지난 뒤의 이력이 실제로 복원된 것이게 한다.
    @Composable
    private fun RestorableContent(content: @Composable (NavBackStack<ScreenNavKey>) -> Unit) {
        val navBackStack =
            rememberSerializable(
                configuration = savedStateConfiguration,
                serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
            ) { NavBackStack<ScreenNavKey>(RoutineHomeNavKey) }
        currentBackStack = navBackStack
        content(navBackStack)
    }

    private fun filterTag(): Tag {
        val tag = Arb.tag(title = TAG_TITLE).next()
        val routine = routineListDetailRoutine(title = ROUTINE_TITLE, today = TODAY)
        environment.tagList = listOf(tag)
        environment.routineTagMap = mapOf(routine.id to setOf(tag.id))
        environment.setRoutineList(routineList = listOf(routine))

        return tag
    }

    // 필터 선택은 기기에 저장되므로 새로 만든 ViewModel도 같은 선택을 읽는다.
    private fun openFilterAndSelect(tag: Tag) {
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tag.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
    }

    private fun assertFilterOpened(tag: Tag) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tag.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.runOnIdle { currentBackStack.toList() } shouldBe listOf(RoutineHomeNavKey, RoutineHomeFilterNavKey)
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val TAG_TITLE = "filter-tag"
        const val ROUTINE_TITLE = "filter-routine"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
        const val TIMEOUT_MILLIS = 5_000L

        val savedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { routineNavKeys() }
                    }
            }
    }
}
