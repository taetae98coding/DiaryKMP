package io.github.taetae98coding.diary.feature.calendar.ui.home

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.app.ActivityOptionsCompat
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenLocationPermissionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-LOCATION-PERMISSION-FEATURE-001 위치 권한이 허용되어 있지 않으면 캘린더 홈 화면 진입 시 시스템 위치 권한 요청이 시작된다`() {
        val registry = PermissionResultRegistry(result = DENIED_RESULT)

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel(),
        )
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
    }

    @Test
    fun `TC-LOCATION-PERMISSION-FEATURE-002 위치 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val registry = PermissionResultRegistry(result = GRANTED_RESULT)
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        registry.launchedPermissionList.shouldBeEmpty()
        verify(exactly = 1) { weatherViewModel.fetch() }
    }

    @Test
    fun `TC-LOCATION-PERMISSION-FEATURE-003 요청을 허용해도 캘린더 홈 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = GRANTED_RESULT)

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel(),
        )
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-LOCATION-PERMISSION-FEATURE-003 요청을 거부해도 캘린더 홈 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val registry = PermissionResultRegistry(result = DENIED_RESULT)

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel(),
        )
        composeRule.waitForIdle()

        registry.launchedPermissionList shouldHaveSize 1
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-025 위치 권한 요청에서 정확한 위치를 허용하면 현재 위치의 날씨를 다시 동기화한다`() {
        val registry = PermissionResultRegistry(result = GRANTED_RESULT)
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        verify(exactly = 1) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-025 위치 권한 요청에서 대략적인 위치만 허용해도 현재 위치의 날씨를 다시 동기화한다`() {
        val registry = PermissionResultRegistry(result = COARSE_ONLY_GRANTED_RESULT)
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        verify(exactly = 1) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-037 위치 권한이 이미 허용되어 있으면 권한을 계기로 날씨를 다시 동기화하지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val registry = PermissionResultRegistry(result = GRANTED_RESULT)
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        verify(exactly = 0) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-026 위치 권한 요청을 거부하면 날씨를 다시 동기화하지 않는다`() {
        val registry = PermissionResultRegistry(result = DENIED_RESULT)
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        verify(exactly = 0) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-052 권한 허용 시점에 날씨 동기화가 진행 중이면 그 동기화가 끝난 뒤 한 번 더 동기화한다`() {
        val fetchGate = CompletableDeferred<Unit>()
        val fetchCurrentWeatherUseCase = mockk<FetchCurrentWeatherUseCase>()
        coEvery { fetchCurrentWeatherUseCase(parameter = Unit) } coAnswers {
            fetchGate.await()
            Result.success(Unit)
        }
        val refreshCurrentWeatherUseCase = mockk<RefreshCurrentWeatherUseCase>()
        coEvery { refreshCurrentWeatherUseCase(parameter = Unit) } returns Result.success(Unit)
        val getCurrentCalendarWeatherUseCase = mockk<GetCurrentCalendarWeatherUseCase>()
        every { getCurrentCalendarWeatherUseCase(parameter = Unit) } returns flowOf(Result.success(CalendarWeatherReport()))
        val weatherViewModel =
            CalendarHomeWeatherViewModel(
                fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
                refreshCurrentWeatherUseCase = refreshCurrentWeatherUseCase,
                getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase,
            )
        val registry = PermissionResultRegistry(result = GRANTED_RESULT)

        setCalendarHomeScreen(
            registry = registry,
            weatherViewModel = weatherViewModel,
        )
        composeRule.waitForIdle()

        coVerify(exactly = 1) { fetchCurrentWeatherUseCase(parameter = Unit) }
        coVerify(exactly = 0) { refreshCurrentWeatherUseCase(parameter = Unit) }

        fetchGate.complete(Unit)
        composeRule.waitForIdle()

        coVerify(exactly = 1) { refreshCurrentWeatherUseCase(parameter = Unit) }
        coVerify(exactly = 1) { fetchCurrentWeatherUseCase(parameter = Unit) }
    }

    private fun setCalendarHomeScreen(
        registry: ActivityResultRegistry,
        weatherViewModel: CalendarHomeWeatherViewModel,
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(emptyList())
                every { viewModel.isFetching } returns MutableStateFlow(false)
            }
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                    CalendarHomeScreen(
                        navigateToMemoDetail = {},
                        navigateToMemoAdd = {},
                        navigateToContactDetail = {},
                        birthdayViewModel = birthdayViewModel(),
                        navigateToFilter = {},
                        state = state,
                        holidayViewModel = holidayViewModel,
                        memoViewModel = memoViewModel,
                        weatherViewModel = weatherViewModel,
                        syncViewModel = syncViewModel(),
                        permissionManager = rememberPermissionManager(),
                    )
                }
            }
        }
    }

    private class PermissionResultRegistry(
        private val result: Map<String, Boolean>,
    ) : ActivityResultRegistry() {
        val launchedPermissionList = mutableListOf<List<String>>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launchedPermissionList += (input as Array<*>).map { permission -> permission.toString() }
            dispatchResult(requestCode, result)
        }
    }

    companion object {
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val GRANTED_RESULT =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to true,
                Manifest.permission.ACCESS_COARSE_LOCATION to true,
            )
        private val COARSE_ONLY_GRANTED_RESULT =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to true,
            )
        private val DENIED_RESULT =
            mapOf(
                Manifest.permission.ACCESS_FINE_LOCATION to false,
                Manifest.permission.ACCESS_COARSE_LOCATION to false,
            )
    }
}
