package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class GetGoldenHolidayUseCaseDispatcherTest :
    BehaviorSpec({
        Given("황금연휴 계산을 맡을 dispatcher가 주입되어 있다") {
            When("황금연휴를 조회한다") {
                Then("계산은 모으는 쪽이 아니라 주입된 dispatcher로 옮겨 실행된다") {
                    runTest {
                        // 날짜로 바꿀 수 있는 연도여야 하므로 표현 범위가 정해진 시각에서 연도를 뽑는다.
                        val year =
                            fixtureMonkey
                                .giveMeOne<Instant>()
                                .toLocalDateTime(TimeZone.UTC)
                                .year
                        val repository = mockk<HolidayRepository>()
                        every { repository.get(countrySet = KOREA_COUNTRY_SET, year = any()) } returns flowOf(emptyList())
                        val calculationDispatcher = spyk(StandardTestDispatcher(testScheduler))
                        val useCase =
                            GetGoldenHolidayUseCase(
                                getHolidayUseCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository),
                                dispatcher = calculationDispatcher,
                            )

                        useCase(parameter = GetGoldenHolidayUseCase.Parameter(year = year, annualLeaveCount = NO_ANNUAL_LEAVE)).test {
                            awaitItem().shouldBeSuccess()
                            awaitComplete()
                        }

                        verify(atLeast = 1) { calculationDispatcher.dispatch(any(), any()) }
                    }
                }
            }
        }
    })

// 연차 수는 계산이 옮겨지는지와 무관하고, 늘리면 조합만 많아져 테스트가 느려진다.
private const val NO_ANNUAL_LEAVE = 0
