package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.calendarEntry
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeSyncViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.time.Instant

// 필터는 캘린더 위에 쌓이는 별도 화면이라, 제품 calendarEntry와 앱과 같은 장면 전략으로 캘린더 목적지를 띄운 채 경계를 지난다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeFilterOpenBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey, CalendarHomeFilterNavKey)
    private val filterViewModelList = mutableListOf<CalendarHomeFilterViewModel>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-094 필터를 연 채 화면이 재생성되어도 필터는 열린 채로 남고 태그 선택이 유지된다`() {
        val tag = filterTag()
        val viewModelStoreOwner = testViewModelStoreOwner()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            CalendarDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-094 필터를 연 채 앱이 백그라운드에 다녀와도 필터는 열린 채로 남고 태그 선택이 유지된다`() {
        val tag = filterTag()
        val viewModelStoreOwner = testViewModelStoreOwner()
        composeRule.setContent {
            CalendarDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-094 필터를 연 채 시스템이 앱을 정리한 뒤 다시 만들면 필터가 열린 채로 다시 보이고 태그 선택이 유지된다`() {
        val tag = filterTag()
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 ViewModel도 사라지므로, 다시 만들 때 새 소유자에서 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModelStoreOwner = remember { testViewModelStoreOwner() }
            CalendarDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
        filterViewModelList.size shouldBe 2
    }

    @Composable
    private fun CalendarDestination(
        tag: Tag,
        viewModelStoreOwner: ViewModelStoreOwner,
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(viewModelModule(tag = tag)) }) {
                DiaryTheme {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(remember { BottomSheetSceneStrategy<ScreenNavKey>() }),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                            ),
                        entryProvider =
                            entryProvider {
                                calendarEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                            },
                    )
                }
            }
        }
    }

    private fun viewModelModule(tag: Tag) =
        module {
            // 다시 만들 때 앞선 ViewModel이 정리되므로 정리 호출에도 답하도록 느슨한 mock으로 둔다.
            factory<CalendarHomeHolidayViewModel> {
                mockk<CalendarHomeHolidayViewModel>(relaxed = true) {
                    every { holidayList } returns MutableStateFlow(emptyList())
                    every { isFetching } returns MutableStateFlow(false)
                }
            }
            factory<CalendarHomeMemoViewModel> {
                mockk<CalendarHomeMemoViewModel>(relaxed = true) {
                    every { memoList } returns MutableStateFlow(emptyList())
                    every { filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState(isApplied = true))
                }
            }
            factory<CalendarHomeBirthdayViewModel> {
                mockk<CalendarHomeBirthdayViewModel>(relaxed = true) {
                    every { birthdayList } returns MutableStateFlow(emptyList())
                }
            }
            factory<CalendarHomeWeatherViewModel> {
                mockk<CalendarHomeWeatherViewModel>(relaxed = true) {
                    every { weatherReport } returns MutableStateFlow(CalendarWeatherReport())
                    every { isLoading } returns MutableStateFlow(false)
                }
            }
            factory<CalendarHomeSyncViewModel> {
                mockk<CalendarHomeSyncViewModel>(relaxed = true) {
                    every { isRefreshing } returns MutableStateFlow(false)
                }
            }
            factory<CalendarHomeFilterViewModel> {
                // 필터 선택은 기기에 저장되어 있으므로 새로 만든 ViewModel도 같은 선택을 읽는다.
                mockk<CalendarHomeFilterViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(CalendarHomeFilterUiState(selectedTagIdSet = setOf(tag.id)))
                    every { tagPagingData } returns flowOf(PagingData.from(listOf(tag)))
                }.also { viewModel -> filterViewModelList += viewModel }
            }
        }

    private fun assertFilterOpened(tag: Tag) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tag.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun filterTag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = "filter-tag-${fixtureMonkey.giveMeOne<String>()}"))
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        fun testViewModelStoreOwner(): ViewModelStoreOwner =
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
    }
}
