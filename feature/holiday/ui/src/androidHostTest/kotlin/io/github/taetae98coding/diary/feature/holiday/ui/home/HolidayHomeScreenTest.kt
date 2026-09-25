package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_LOADING_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_RETRY_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_RETRY_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-003 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-010 연차 개수를 바꾸면 황금연휴 목록이 다시 표시된다`() {
        val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
        every { getGoldenHolidayUseCase(parameter = any()) } answers {
            val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()
            val groupList =
                if (parameter.year == YEAR && parameter.annualLeaveCount > 0) {
                    listOf(annualLeaveGoldenHolidayGroup())
                } else {
                    emptyList()
                }

            flowOf(Result.success(groupList))
        }
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            getGoldenHolidayUseCase = getGoldenHolidayUseCase,
        )

        composeRule.onNodeWithText("Feb 5 ~ Feb 8").assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Feb 5 ~ Feb 8").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-026 공휴일이 준비되는 동안 로딩 표시가 나타난다`() {
        val completion = CompletableDeferred<Result<List<Holiday>>>()
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = suspendedFetchHolidayUseCase(completion = completion),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertDoesNotExist()

        completion.complete(Result.success(providedHolidayList()))
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-027 준비가 끝나면 별도 조작 없이 본문이 목록으로 바뀐다`() {
        val completion = CompletableDeferred<Result<List<Holiday>>>()
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = suspendedFetchHolidayUseCase(completion = completion),
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertExists()

        completion.complete(Result.success(providedHolidayList()))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-028 기본 환경에서 공휴일을 받아오지 못하면 오류 안내와 재시도 동작이 표시된다`() {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = previousYearFailFetchHolidayUseCase(),
        )

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).assertExists()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-HOLIDAY-HOME-FEATURE-028 한국어 환경에서 공휴일을 받아오지 못하면 오류 안내와 재시도 동작이 표시된다`() {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = previousYearFailFetchHolidayUseCase(),
        )

        composeRule.onNodeWithText(KOREAN_ERROR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_RETRY_LABEL).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-029 재시도를 선택하면 다시 받아와 목록이 표시된다`() {
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

        composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    private companion object {
        private const val HOLIDAY_NAME = "공휴일"

        private fun suspendedFetchHolidayUseCase(completion: CompletableDeferred<Result<List<Holiday>>>): FetchHolidayUseCase =
            mockk<FetchHolidayUseCase>().also { useCase ->
                coEvery { useCase(parameter = any()) } coAnswers { completion.await() }
            }

        // 2026년 2월 5일 목요일 공휴일에 연차 하루를 붙인 2월 5일부터 8일까지의 연휴다.
        private fun annualLeaveGoldenHolidayGroup(): GoldenHolidayGroup =
            goldenHolidayGroup(
                optionList =
                    listOf(
                        goldenHoliday(
                            holidayList = listOf(holiday(name = HOLIDAY_NAME, start = february(day = 5))),
                            start = february(day = 5),
                            endInclusive = february(day = 8),
                            annualLeaveDateRangeList = listOf(february(day = 6)..february(day = 6)),
                        ),
                    ),
            )
    }
}
