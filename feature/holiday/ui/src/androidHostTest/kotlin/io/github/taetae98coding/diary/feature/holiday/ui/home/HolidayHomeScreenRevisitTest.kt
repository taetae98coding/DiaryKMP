package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_LOADING_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NOT_PROVIDED_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScreenRevisitTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-030 한 년도의 오류는 다른 년도의 표시에 영향을 주지 않는다`() {
        val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
        every { getGoldenHolidayUseCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList =
                if (parameter.year == YEAR + 1) {
                    listOf(nextYearGoldenHolidayGroup())
                } else {
                    emptyList()
                }

            flowOf(Result.success(groupList))
        }
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = previousYearFailFetchHolidayUseCase(),
            getGoldenHolidayUseCase = getGoldenHolidayUseCase,
        )

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertExists()

        scrollTo(year = YEAR + 1)

        composeRule.onNodeWithText(NEXT_YEAR_PERIOD).assertExists()
        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-058 한 년도의 제공 없음은 다른 년도의 표시에 영향을 주지 않는다`() {
        val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
        every { getGoldenHolidayUseCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList =
                if (parameter.year == YEAR + 1) {
                    listOf(nextYearGoldenHolidayGroup())
                } else {
                    emptyList()
                }

            flowOf(Result.success(groupList))
        }
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = thisYearNotProvidedFetchHolidayUseCase(),
            getGoldenHolidayUseCase = getGoldenHolidayUseCase,
        )

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertExists()

        scrollTo(year = YEAR + 1)

        composeRule.onNodeWithText(NEXT_YEAR_PERIOD).assertExists()
        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-031 완료된 년도로 다시 이동하면 로딩 없이 곧바로 목록이 표시된다`() {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()

        scrollTo(year = YEAR + 1)
        scrollTo(year = YEAR)

        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-033 다시 동기화하는 동안에도 이미 표시된 목록은 유지된다`() {
        val completion = CompletableDeferred<Result<List<Holiday>>>()
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()

        // 이후의 동기화는 끝나지 않는다.
        coEvery { fetchHolidayUseCase(parameter = any()) } coAnswers { completion.await() }
        scrollTo(year = YEAR + 1)
        scrollTo(year = YEAR)

        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertDoesNotExist()

        completion.complete(Result.success(providedHolidayList()))
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-034 오류 년도로 다시 이동하면 다시 동기화한다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { fetchHolidayUseCase(parameter = YEAR - 1) } returnsMany
            listOf(
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                Result.success(providedHolidayList()),
            )
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertExists()

        scrollTo(year = YEAR + 1)
        scrollTo(year = YEAR)

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-044 제공 없음 년도로 다시 이동하면 다시 동기화한다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { fetchHolidayUseCase(parameter = YEAR) } returnsMany
            listOf(
                Result.success(emptyList()),
                Result.success(providedHolidayList()),
            )
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertExists()

        scrollTo(year = YEAR + 1)
        scrollTo(year = YEAR)

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    private fun scrollTo(year: Int) {
        composeRule.runOnIdle { targetYear.value = year }
        composeRule.waitForIdle()
    }
}
