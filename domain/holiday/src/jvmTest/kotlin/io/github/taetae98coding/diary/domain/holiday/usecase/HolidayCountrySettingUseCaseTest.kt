package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.DeviceCountryRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private data class CountryCase(
    val optionSet: Set<HolidayCountryOption>,
    val deviceCountry: HolidayCountry?,
    val countrySet: Set<HolidayCountry>,
)

private data class ToggleCase(
    val initialOptionSet: Set<HolidayCountryOption>,
    val option: HolidayCountryOption,
    val isAdded: Boolean,
)

class HolidayCountrySettingUseCaseTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-COUNTRY-DOMAIN-001 저장소가 기기값만 고른 기본 상태를 제공한다") {
            val deviceCountry = fixtureMonkey.giveMeOne<HolidayCountry>()
            val useCase = countrySettingUseCase(optionSet = setOf(HolidayCountryOption.DEVICE), deviceCountry = deviceCountry)

            When("고른 국가 선택지를 조회한다") {
                Then("기기값만 고른 상태로 제공된다") {
                    val setting = useCase(parameter = Unit).first().shouldBeSuccess()

                    setting.selectedOptionSet shouldBe setOf(HolidayCountryOption.DEVICE)
                    setting.deviceCountry shouldBe deviceCountry
                }
            }
        }

        Given("TC-HOLIDAY-COUNTRY-DOMAIN-002 고른 선택지와 기기 지역이 준비되어 있다") {
            val caseList =
                listOf(
                    CountryCase(setOf(HolidayCountryOption.DEVICE), HolidayCountry.KOREA, setOf(HolidayCountry.KOREA)),
                    CountryCase(setOf(HolidayCountryOption.DEVICE), HolidayCountry.UNITED_STATES, setOf(HolidayCountry.UNITED_STATES)),
                    CountryCase(setOf(HolidayCountryOption.DEVICE), null, emptySet()),
                    CountryCase(setOf(HolidayCountryOption.KOREA), HolidayCountry.UNITED_STATES, setOf(HolidayCountry.KOREA)),
                    CountryCase(setOf(HolidayCountryOption.UNITED_STATES), HolidayCountry.KOREA, setOf(HolidayCountry.UNITED_STATES)),
                    CountryCase(
                        setOf(HolidayCountryOption.KOREA, HolidayCountryOption.UNITED_STATES),
                        null,
                        setOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES),
                    ),
                    CountryCase(setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.KOREA), HolidayCountry.KOREA, setOf(HolidayCountry.KOREA)),
                    CountryCase(
                        setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.KOREA),
                        HolidayCountry.UNITED_STATES,
                        setOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES),
                    ),
                    CountryCase(emptySet(), HolidayCountry.KOREA, emptySet()),
                )

            When("적용 국가를 조회한다") {
                Then("테스트 데이터의 적용 국가가 제공된다") {
                    caseList.forEach { case ->
                        countrySettingUseCase(optionSet = case.optionSet, deviceCountry = case.deviceCountry)(parameter = Unit)
                            .first()
                            .shouldBeSuccess()
                            .countrySet shouldBe case.countrySet
                    }
                }
            }
        }

        Given("TC-HOLIDAY-COUNTRY-DOMAIN-003 기기값만 고른 상태이고 기기 지역이 한국이다") {
            val deviceCountryRepository = mockk<DeviceCountryRepository>()
            every { deviceCountryRepository.find() } returnsMany listOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES)
            val useCase =
                GetHolidayCountrySettingUseCase(
                    holidaySettingRepository =
                        mockk<HolidaySettingRepository>().also { repository ->
                            every { repository.getCountryOptionSet() } returns flowOf(setOf(HolidayCountryOption.DEVICE))
                        },
                    deviceCountryRepository = deviceCountryRepository,
                )

            When("기기 지역을 미국으로 바꾼 뒤 적용 국가를 다시 조회한다") {
                val before = useCase(parameter = Unit).first().shouldBeSuccess().countrySet
                val after = useCase(parameter = Unit).first().shouldBeSuccess().countrySet

                Then("적용 국가는 미국이다") {
                    before shouldBe setOf(HolidayCountry.KOREA)
                    after shouldBe setOf(HolidayCountry.UNITED_STATES)
                }
            }
        }

        Given("TC-HOLIDAY-COUNTRY-DATA-003 고른 선택지를 조회하고 있다") {
            val optionSetFlow = MutableStateFlow(setOf(HolidayCountryOption.DEVICE))
            val useCase = countrySettingUseCase(optionSetFlow = optionSetFlow, deviceCountry = HolidayCountry.KOREA)

            When("선택지 하나를 바꾼다") {
                Then("바뀐 선택지가 이어서 제공된다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess().selectedOptionSet shouldBe setOf(HolidayCountryOption.DEVICE)

                        optionSetFlow.value = setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.UNITED_STATES)

                        awaitItem().shouldBeSuccess().selectedOptionSet shouldBe setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.UNITED_STATES)
                    }
                }
            }
        }

        Given("TC-HOLIDAY-COUNTRY-DATA-001 테스트 데이터의 선택지를 고른 상태다") {
            val caseList =
                listOf(
                    ToggleCase(setOf(HolidayCountryOption.DEVICE), HolidayCountryOption.KOREA, isAdded = true),
                    ToggleCase(setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.KOREA), HolidayCountryOption.DEVICE, isAdded = false),
                    ToggleCase(setOf(HolidayCountryOption.KOREA, HolidayCountryOption.UNITED_STATES), HolidayCountryOption.UNITED_STATES, isAdded = false),
                    ToggleCase(setOf(HolidayCountryOption.DEVICE), HolidayCountryOption.DEVICE, isAdded = false),
                )

            When("테스트 데이터의 선택지를 바꾼다") {
                Then("없던 선택지는 더하고 있던 선택지는 빼도록 저장을 요청한다") {
                    caseList.forEach { case ->
                        val repository = mockk<HolidaySettingRepository>()
                        every { repository.getCountryOptionSet() } returns flowOf(case.initialOptionSet)
                        coEvery { repository.addCountryOption(option = any()) } returns Unit
                        coEvery { repository.removeCountryOption(option = any()) } returns Unit

                        ToggleHolidayCountryOptionUseCase(holidaySettingRepository = repository)(parameter = case.option).shouldBeSuccess()

                        coVerify(exactly = if (case.isAdded) 1 else 0) { repository.addCountryOption(option = case.option) }
                        coVerify(exactly = if (case.isAdded) 0 else 1) { repository.removeCountryOption(option = case.option) }
                    }
                }
            }
        }
    })
