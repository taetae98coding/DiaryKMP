package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.time.Clock

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal const val THIS_YEAR_PERIOD: String = "Feb 5 ~ Feb 8"
internal const val NEXT_YEAR_PERIOD: String = "Feb 5 ~ Feb 7"

private const val HOLIDAY_NAME = "공휴일"
private const val NEXT_YEAR_HOLIDAY_NAME = "내년 공휴일"

internal fun providedHolidayList(): List<Holiday> = listOf(holiday(name = HOLIDAY_NAME, start = february(day = 5)))

internal fun successfulFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(providedHolidayList())
    }

internal fun previousYearFailFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { useCase(parameter = YEAR - 1) } returns
            Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
    }

internal fun thisYearNotProvidedFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { useCase(parameter = YEAR) } returns Result.success(emptyList())
    }

internal fun emptyGetGoldenHolidayUseCase(): GetGoldenHolidayUseCase =
    mockk<GetGoldenHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
    }

internal fun thisYearGetGoldenHolidayUseCase(year: Int = YEAR): GetGoldenHolidayUseCase =
    mockk<GetGoldenHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList =
                if (parameter.year == year) {
                    listOf(thisYearGoldenHolidayGroup(year = year))
                } else {
                    emptyList()
                }

            flowOf(Result.success(groupList))
        }
    }

// 2월 5일 공휴일로 2월 5일부터 8일까지가 연휴인 응답이다. 2026년이면 2월 5일이 목요일이라 실제 연휴와도 같다.
private fun thisYearGoldenHolidayGroup(year: Int): GoldenHolidayGroup {
    val start = LocalDate(year = year, month = Month.FEBRUARY, day = 5)

    return goldenHolidayGroup(
        optionList =
            listOf(
                goldenHoliday(
                    holidayList = listOf(holiday(name = HOLIDAY_NAME, start = start)),
                    start = start,
                    endInclusive = LocalDate(year = year, month = Month.FEBRUARY, day = 8),
                ),
            ),
    )
}

// 2027년 2월 5일은 금요일이므로 2월 5일부터 7일까지가 연휴다.
internal fun nextYearGoldenHolidayGroup(): GoldenHolidayGroup {
    val start = LocalDate(year = YEAR + 1, month = Month.FEBRUARY, day = 5)
    val endInclusive = LocalDate(year = YEAR + 1, month = Month.FEBRUARY, day = 7)
    val nextYearHoliday =
        Holiday(
            name = NEXT_YEAR_HOLIDAY_NAME,
            isHoliday = true,
            dateRange = LocalDateRange(start = start, endInclusive = start),
        )

    return goldenHolidayGroup(
        optionList =
            listOf(
                goldenHoliday(
                    holidayList = listOf(nextYearHoliday),
                    start = start,
                    endInclusive = endInclusive,
                ),
            ),
    )
}

// 모듈이 호출마다 달라지는 UseCase를 붙잡아야 해 최상위 val로 둘 수 없고, 그래서 KOIN-W003(그래프 컴파일 타임 검증 불가)이 남는다.
// 억제 수단은 compileSafety를 끄는 것뿐인데 이 모듈의 프로덕션 정의 검증까지 잃으므로 경고를 그대로 받아들인다.
private fun holidayHomeYearViewModelModule(
    fetchHolidayUseCase: FetchHolidayUseCase,
    getGoldenHolidayUseCase: GetGoldenHolidayUseCase,
): Module =
    module {
        factory { parametersHolder ->
            HolidayHomeYearViewModel(
                year = parametersHolder.get(),
                fetchHolidayUseCase = fetchHolidayUseCase,
                getGoldenHolidayUseCase = getGoldenHolidayUseCase,
            )
        }
    }

internal fun ComposeContentTestRule.setHolidayHomeScreen(
    targetYear: State<Int?>,
    fetchHolidayUseCase: FetchHolidayUseCase = successfulFetchHolidayUseCase(),
    getGoldenHolidayUseCase: GetGoldenHolidayUseCase = emptyGetGoldenHolidayUseCase(),
    navigateUp: () -> Unit = {},
    navigateToMemoAdd: (LocalDateRange) -> Unit = {},
    lifecycleOwner: () -> LifecycleOwner? = { null },
    restorationTester: StateRestorationTester? = null,
    launchCount: State<Int>? = null,
    clock: Clock? = null,
) {
    val setContent: (@Composable () -> Unit) -> Unit = restorationTester?.let { tester -> tester::setContent } ?: this::setContent
    // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
    // 화면 재생성은 구성만 다시 만들고 소유자는 넘겨받으므로 구성 바깥에 두고, 다시 진입하거나 다시 실행한 화면만 새 소유자를 받는다.
    val viewModelStoreOwnerMap = mutableMapOf<Int?, ViewModelStoreOwner>()

    // launchCount가 바뀌면 저장된 상태 없이 화면을 새로 구성해, 화면에 다시 진입하거나 앱을 다시 실행한 것처럼 만든다.
    setContent {
        key(launchCount?.value) {
            val viewModelStoreOwner =
                remember {
                    viewModelStoreOwnerMap.getOrPut(launchCount?.value) {
                        object : ViewModelStoreOwner {
                            override val viewModelStore: ViewModelStore = ViewModelStore()
                        }
                    }
                }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalLifecycleOwner provides (lifecycleOwner() ?: LocalLifecycleOwner.current),
            ) {
                KoinApplication(
                    configuration =
                        koinConfiguration {
                            modules(holidayHomeYearViewModelModule(fetchHolidayUseCase, getGoldenHolidayUseCase))
                        },
                ) {
                    val state =
                        if (clock == null) {
                            rememberHolidayHomeScaffoldState(initialYear = YEAR)
                        } else {
                            rememberHolidayHomeScaffoldState(clock = clock)
                        }

                    ScrollToYearEffect(
                        state = state,
                        targetYear = targetYear,
                    )
                    DiaryTheme {
                        HolidayHomeScreen(
                            navigateUp = navigateUp,
                            navigateToMemoAdd = navigateToMemoAdd,
                            state = state,
                        )
                    }
                }
            }
        }
    }
    waitForIdle()
}

@Composable
private fun ScrollToYearEffect(
    state: HolidayHomeScaffoldState,
    targetYear: State<Int?>,
) {
    val year = targetYear.value

    LaunchedEffect(year) {
        year?.let { state.animateScrollTo(year = it) }
    }
}
