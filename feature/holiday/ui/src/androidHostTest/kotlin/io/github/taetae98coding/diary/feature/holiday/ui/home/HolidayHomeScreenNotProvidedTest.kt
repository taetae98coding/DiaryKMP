package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NOT_PROVIDED_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_RETRY_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_NOT_PROVIDED_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_RETRY_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.mockk
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
class HolidayHomeScreenNotProvidedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-c81a945f-ccb9-4404-a497-9294e344a234 기본 환경에서 표시 년도의 공휴일이 제공되지 않으면 제공 없음 안내가 표시된다`() {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = thisYearNotProvidedFetchHolidayUseCase(),
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-c81a945f-ccb9-4404-a497-9294e344a234 한국어 환경에서 표시 년도의 공휴일이 제공되지 않으면 제공 없음 안내가 표시된다`() {
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = thisYearNotProvidedFetchHolidayUseCase(),
        )

        composeRule.onNodeWithText(KOREAN_NOT_PROVIDED_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_RETRY_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-d1743b57-785c-49f9-a78c-beef6e217946 적용 국가가 없으면 제공 없음 안내가 표시된다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(emptyList())
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
        )

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertDoesNotExist()
    }

    @Test
    fun `TC-198e0061-10ba-4b7a-a2e7-aca0b9977590 앞뒤 년도만 제공되지 않으면 표시 년도의 목록이 표시된다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(emptyList())
        coEvery { fetchHolidayUseCase(parameter = YEAR) } returns Result.success(providedHolidayList())
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
            getGoldenHolidayUseCase = thisYearGetGoldenHolidayUseCase(),
        )

        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(THIS_YEAR_PERIOD).assertExists()
    }

    @Test
    fun `TC-ee60a401-4b78-40ab-8314-46139a915610 받아오지 못한 판단 대상 년도가 있으면 오류 안내가 표시된다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
        coEvery { fetchHolidayUseCase(parameter = YEAR) } returns Result.success(emptyList())
        coEvery { fetchHolidayUseCase(parameter = YEAR - 1) } returns
            Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        composeRule.setHolidayHomeScreen(
            targetYear = targetYear,
            fetchHolidayUseCase = fetchHolidayUseCase,
        )

        composeRule.onNodeWithText(DEFAULT_ERROR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_NOT_PROVIDED_DESCRIPTION).assertDoesNotExist()
    }
}
