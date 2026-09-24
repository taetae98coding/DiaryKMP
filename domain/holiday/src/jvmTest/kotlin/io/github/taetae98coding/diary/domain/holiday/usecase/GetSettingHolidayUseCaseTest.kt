package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetSettingHolidayUseCaseTest :
    BehaviorSpec({
        Given("저장된 전체 공휴일과 숨김 key가 있다") {
            val holidayList = listOf(holiday())
            val hiddenKeySet = setOf(holidayList.single().name.toHolidayKey())
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = flowOf(holidayList),
                    hiddenKeySetFlow = flowOf(hiddenKeySet),
                )

            When("설정 화면용 공휴일을 조회한다") {
                Then("전체 공휴일과 숨김 key를 결합한 결과를 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe
                            holidayList.toHolidaySettingList(hiddenKeySet = hiddenKeySet)
                        awaitComplete()
                    }
                }
            }
        }

        Given("설정 화면용 공휴일을 계속 조회하고 있다") {
            val initialHolidayList = listOf(holiday(name = uniqueName(suffix = "initial")))
            val changedHolidayList =
                listOf(
                    holiday(name = uniqueName(suffix = "changed-first")),
                    holiday(name = uniqueName(suffix = "changed-second")),
                )
            val holidayFlow = MutableStateFlow(initialHolidayList)
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = holidayFlow,
                    hiddenKeySetFlow = flowOf(emptySet()),
                )

            When("저장된 전체 공휴일이 바뀐다") {
                Then("바뀐 공휴일로 구성한 결과를 이어서 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe
                            initialHolidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                        holidayFlow.value = changedHolidayList

                        awaitItem().shouldBeSuccess() shouldBe
                            changedHolidayList.toHolidaySettingList(hiddenKeySet = emptySet())
                    }
                }
            }
        }

        Given("숨기지 않은 공휴일을 설정 화면용으로 계속 조회하고 있다") {
            val holidayList = listOf(holiday())
            val hiddenKeySetFlow = MutableStateFlow<Set<String>>(emptySet())
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = flowOf(holidayList),
                    hiddenKeySetFlow = hiddenKeySetFlow,
                )

            When("숨김 key가 바뀐다") {
                Then("바뀐 숨김 상태를 적용한 결과를 이어서 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe
                            holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                        val changedHiddenKeySet = setOf(holidayList.single().name.toHolidayKey())
                        hiddenKeySetFlow.value = changedHiddenKeySet

                        awaitItem().shouldBeSuccess() shouldBe
                            holidayList.toHolidaySettingList(hiddenKeySet = changedHiddenKeySet)
                    }
                }
            }
        }

        Given("저장된 공휴일이 없다") {
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = flowOf(emptyList()),
                    hiddenKeySetFlow = flowOf(setOf(fixtureMonkey.giveMeOne())),
                )

            When("설정 화면용 공휴일을 조회한다") {
                Then("빈 목록을 성공 결과로 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess() shouldBe emptyList()
                        awaitComplete()
                    }
                }
            }
        }

        Given("저장된 전체 공휴일을 읽을 수 없다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = flow { throw failure },
                    hiddenKeySetFlow = flowOf(emptySet()),
                )

            When("설정 화면용 공휴일을 조회한다") {
                Then("전체 공휴일을 읽지 못한 원인을 그대로 실패로 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure().shouldHaveRootCause(failure)
                        awaitComplete()
                    }
                }
            }
        }

        Given("숨김 key를 읽을 수 없다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val useCase =
                getSettingHolidayUseCase(
                    holidayFlow = flowOf(listOf(holiday())),
                    hiddenKeySetFlow = flow { throw failure },
                )

            When("설정 화면용 공휴일을 조회한다") {
                Then("숨김 key를 읽지 못한 원인을 그대로 실패로 제공한다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure().shouldHaveRootCause(failure)
                        awaitComplete()
                    }
                }
            }
        }
    })

private fun getSettingHolidayUseCase(
    holidayFlow: Flow<List<Holiday>>,
    hiddenKeySetFlow: Flow<Set<String>>,
): GetSettingHolidayUseCase =
    GetSettingHolidayUseCase(
        getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
        holidayRepository =
            mockk<HolidayRepository>().also { repository ->
                every { repository.get(countrySet = KOREA_COUNTRY_SET) } returns holidayFlow
            },
        holidaySettingRepository =
            mockk<HolidaySettingRepository>().also { repository ->
                every { repository.getHiddenKeySet() } returns hiddenKeySetFlow
            },
    )

private fun holiday(name: String = fixtureMonkey.giveMeOne()): Holiday {
    val start = randomDate()

    return Holiday(
        name = name,
        isHoliday = fixtureMonkey.giveMeOne(),
        dateRange = start..start,
    )
}

private fun uniqueName(suffix: String): String = "${fixtureMonkey.giveMeOne<String>()}-$suffix"

private fun Throwable.shouldHaveRootCause(expected: Throwable) {
    generateSequence(this) { throwable -> throwable.cause }
        .last() shouldBeSameInstanceAs expected
}

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
