package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetHolidayUseCaseTest :
    BehaviorSpec({
        Given("특정 연도에 공휴일이 저장되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val holidayList = listOf(holiday(), holiday())
            val repository = mockk<HolidayRepository>()
            every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns flowOf(holidayList)
            val useCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("해당 연도의 공휴일을 조회한다") {
                Then("저장된 공휴일을 성공 결과로 제공한다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe holidayList
                        awaitComplete()
                    }
                }
            }
        }

        Given("특정 연도에 저장된 공휴일이 없다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns flowOf(emptyList())
            val useCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("해당 연도의 공휴일을 조회한다") {
                Then("빈 목록을 성공 결과로 제공한다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe emptyList()
                        awaitComplete()
                    }
                }
            }
        }

        Given("조회 중인 연도의 공휴일이 바뀐다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val holidayList = listOf(holiday())
            val changedHolidayList = listOf(holiday(), holiday())
            val holidayFlow = MutableStateFlow(holidayList)
            val repository = mockk<HolidayRepository>()
            every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns holidayFlow
            val useCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("해당 연도의 공휴일을 계속 조회한다") {
                Then("바뀐 공휴일을 이어서 제공한다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe holidayList
                        holidayFlow.value = changedHolidayList
                        awaitItem().shouldBeSuccess() shouldBe changedHolidayList
                    }
                }
            }
        }

        Given("공휴일 조회가 실패하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<HolidayRepository>()
            every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns flow { throw failure }
            val useCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("해당 연도의 공휴일을 조회한다") {
                Then("실패 원인을 그대로 제공한다") {
                    useCase(parameter = year).test {
                        // flatMapLatest를 거치면 코루틴 스택 복원이 원인을 감싼 사본을 만들 수 있어 원래 원인을 비교한다.
                        val throwable = awaitItem().shouldBeFailure()
                        (throwable.cause ?: throwable) shouldBeSameInstanceAs failure
                        awaitComplete()
                    }
                }
            }
        }
    })

private fun holiday(): Holiday {
    val start = randomDate()

    return Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        dateRange = start..start,
    )
}

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
