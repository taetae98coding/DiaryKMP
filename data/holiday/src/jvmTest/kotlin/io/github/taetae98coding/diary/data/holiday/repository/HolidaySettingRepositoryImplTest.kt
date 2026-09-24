package io.github.taetae98coding.diary.data.holiday.repository

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

private const val MIDSUMMER_DAY_KEY = "초복"
private const val CONSTITUTION_DAY_KEY = "제헌절"
private const val SECOND_MIDSUMMER_DAY_KEY = "중복"

class HolidaySettingRepositoryImplTest :
    FunSpec({
        test("보관된 노출 설정을 읽지 못하면 실패를 그대로 전파한다") {
            val failure = IllegalStateException()
            val repository =
                HolidaySettingRepositoryImpl(
                    holidaySettingLocalDataSource =
                        mockk<HolidaySettingLocalDataSource>().also { dataSource ->
                            every { dataSource.getHiddenKeySet() } returns flow { throw failure }
                        },
                )

            shouldThrow<IllegalStateException> { repository.getHiddenKeySet().first() } shouldBeSameInstanceAs failure
        }

        test("보관된 숨김 key를 제공하던 중에 읽지 못하면 실패를 그대로 전파한다") {
            val failure = IllegalStateException()
            val repository =
                HolidaySettingRepositoryImpl(
                    holidaySettingLocalDataSource =
                        mockk<HolidaySettingLocalDataSource>().also { dataSource ->
                            every { dataSource.getHiddenKeySet() } returns
                                flow {
                                    emit(setOf(MIDSUMMER_DAY_KEY))
                                    throw failure
                                }
                        },
                )

            repository.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(MIDSUMMER_DAY_KEY)
                awaitError() shouldBeSameInstanceAs failure
            }
        }

        test("보관된 숨김 key를 그대로 제공한다") {
            val repository =
                HolidaySettingRepositoryImpl(
                    holidaySettingLocalDataSource = mockHolidaySettingLocalDataSource(MutableStateFlow(setOf(MIDSUMMER_DAY_KEY))),
                )

            repository.getHiddenKeySet().first() shouldBe setOf(MIDSUMMER_DAY_KEY)
        }

        test("보관된 숨김 key가 바뀌면 바뀐 값을 이어서 제공한다") {
            val hiddenKeySetFlow = MutableStateFlow(setOf(MIDSUMMER_DAY_KEY))
            val repository =
                HolidaySettingRepositoryImpl(
                    holidaySettingLocalDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow),
                )

            repository.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(MIDSUMMER_DAY_KEY)

                hiddenKeySetFlow.value = setOf(CONSTITUTION_DAY_KEY)

                awaitItem() shouldBe setOf(CONSTITUTION_DAY_KEY)
            }
        }

        test("숨김 key 추가를 그대로 위임한다") {
            val hiddenKeySetFlow = MutableStateFlow(setOf(CONSTITUTION_DAY_KEY))
            val localDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow)
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            repository.addHiddenKey(key = MIDSUMMER_DAY_KEY)

            hiddenKeySetFlow.value shouldBe setOf(CONSTITUTION_DAY_KEY, MIDSUMMER_DAY_KEY)
            coVerify(exactly = 1) { localDataSource.addHiddenKey(key = MIDSUMMER_DAY_KEY) }
        }

        test("숨김 key 제거를 그대로 위임한다") {
            val hiddenKeySetFlow = MutableStateFlow(setOf(CONSTITUTION_DAY_KEY, MIDSUMMER_DAY_KEY))
            val localDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow)
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            repository.removeHiddenKey(key = CONSTITUTION_DAY_KEY)

            hiddenKeySetFlow.value shouldBe setOf(MIDSUMMER_DAY_KEY)
            coVerify(exactly = 1) { localDataSource.removeHiddenKey(key = CONSTITUTION_DAY_KEY) }
        }

        test("숨김 key 집합 제출을 그대로 위임한다") {
            val hiddenKeySetFlow = MutableStateFlow(setOf(MIDSUMMER_DAY_KEY))
            val localDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow)
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)
            val submittedKeySet = setOf(SECOND_MIDSUMMER_DAY_KEY, CONSTITUTION_DAY_KEY)

            repository.submitHiddenKeySet(
                hiddenKeySet = submittedKeySet,
            )

            hiddenKeySetFlow.value shouldBe submittedKeySet
            coVerify(exactly = 1) {
                localDataSource.upsertHiddenKeySet(
                    hiddenKeySet = submittedKeySet,
                )
            }
        }

        test("숨김 key 집합을 제출하면 제출하지 않은 기존 key를 제거한다") {
            val hiddenKeySetFlow = MutableStateFlow(setOf("캐시에없는key", MIDSUMMER_DAY_KEY, SECOND_MIDSUMMER_DAY_KEY))
            val localDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow)
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)
            val submittedKeySet = setOf(SECOND_MIDSUMMER_DAY_KEY, CONSTITUTION_DAY_KEY)

            repository.submitHiddenKeySet(
                hiddenKeySet = submittedKeySet,
            )

            hiddenKeySetFlow.value shouldBe submittedKeySet
            coVerify(exactly = 1) {
                localDataSource.upsertHiddenKeySet(
                    hiddenKeySet = submittedKeySet,
                )
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-012 목표 상태와 같은 일괄 변경은 새 값을 제공하지 않는다") {
            val workingDayKey = SECOND_MIDSUMMER_DAY_KEY
            val hiddenKeySetFlow = MutableStateFlow(setOf(workingDayKey))
            val localDataSource = mockHolidaySettingLocalDataSource(hiddenKeySetFlow)
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            repository.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(workingDayKey)

                repository.submitHiddenKeySet(
                    hiddenKeySet = setOf(workingDayKey),
                )

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) {
                localDataSource.upsertHiddenKeySet(
                    hiddenKeySet = setOf(workingDayKey),
                )
            }
        }

        test("숨김 key 추가 오류를 그대로 전파한다") {
            val failure = IllegalStateException("update failure")
            val localDataSource = mockk<HolidaySettingLocalDataSource>()
            every { localDataSource.getHiddenKeySet() } returns MutableStateFlow(emptySet())
            coEvery { localDataSource.addHiddenKey(key = any()) } throws failure
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            shouldThrowExactly<IllegalStateException> {
                repository.addHiddenKey(key = MIDSUMMER_DAY_KEY)
            } shouldBeSameInstanceAs failure
        }

        test("숨김 key 제거 오류를 그대로 전파한다") {
            val failure = IllegalStateException("update failure")
            val localDataSource = mockk<HolidaySettingLocalDataSource>()
            every { localDataSource.getHiddenKeySet() } returns MutableStateFlow(emptySet())
            coEvery { localDataSource.removeHiddenKey(key = any()) } throws failure
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            shouldThrowExactly<IllegalStateException> {
                repository.removeHiddenKey(key = MIDSUMMER_DAY_KEY)
            } shouldBeSameInstanceAs failure
        }

        test("숨김 key 집합 제출 오류를 그대로 전파한다") {
            val failure = IllegalStateException("update failure")
            val localDataSource = mockk<HolidaySettingLocalDataSource>()
            every { localDataSource.getHiddenKeySet() } returns MutableStateFlow(emptySet())
            coEvery {
                localDataSource.upsertHiddenKeySet(
                    hiddenKeySet = any(),
                )
            } throws failure
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            shouldThrowExactly<IllegalStateException> {
                repository.submitHiddenKeySet(
                    hiddenKeySet = setOf(MIDSUMMER_DAY_KEY),
                )
            } shouldBeSameInstanceAs failure
        }
        test("보관된 국가 선택지를 도메인 선택지로 제공한다") {
            val localDataSource = mockk<HolidaySettingLocalDataSource>()
            every { localDataSource.getCountryOptionSet() } returns
                flowOf(HolidayCountryOptionLocalEntity.entries.toSet())
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            repository.getCountryOptionSet().first() shouldBe HolidayCountryOption.entries.toSet()
        }

        test("국가 선택지를 대응하는 보관 선택지로 추가하고 제거한다") {
            countryOptionPairList.forEach { (option, localOption) ->
                val localDataSource = mockk<HolidaySettingLocalDataSource>()
                coEvery { localDataSource.addCountryOption(option = any()) } just Runs
                coEvery { localDataSource.removeCountryOption(option = any()) } just Runs
                val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

                repository.addCountryOption(option = option)
                repository.removeCountryOption(option = option)

                coVerify(exactly = 1) { localDataSource.addCountryOption(option = localOption) }
                coVerify(exactly = 1) { localDataSource.removeCountryOption(option = localOption) }
            }
        }

        test("보관된 국가 선택지를 읽지 못하면 실패를 그대로 전파한다") {
            val failure = IllegalStateException()
            val localDataSource = mockk<HolidaySettingLocalDataSource>()
            every { localDataSource.getCountryOptionSet() } returns flow { throw failure }
            val repository = HolidaySettingRepositoryImpl(holidaySettingLocalDataSource = localDataSource)

            shouldThrow<IllegalStateException> { repository.getCountryOptionSet().first() } shouldBeSameInstanceAs failure
        }
    })

private val countryOptionPairList: List<Pair<HolidayCountryOption, HolidayCountryOptionLocalEntity>> =
    listOf(
        HolidayCountryOption.DEVICE to HolidayCountryOptionLocalEntity.DEVICE,
        HolidayCountryOption.KOREA to HolidayCountryOptionLocalEntity.KOREA,
        HolidayCountryOption.UNITED_STATES to HolidayCountryOptionLocalEntity.UNITED_STATES,
    )

private fun mockHolidaySettingLocalDataSource(hiddenKeySetFlow: MutableStateFlow<Set<String>>): HolidaySettingLocalDataSource =
    mockk<HolidaySettingLocalDataSource>().also { dataSource ->
        every { dataSource.getHiddenKeySet() } returns hiddenKeySetFlow
        coEvery { dataSource.addHiddenKey(key = any()) } coAnswers {
            hiddenKeySetFlow.value = hiddenKeySetFlow.value + firstArg<String>()
        }
        coEvery { dataSource.removeHiddenKey(key = any()) } coAnswers {
            hiddenKeySetFlow.value = hiddenKeySetFlow.value - firstArg<String>()
        }
        coEvery {
            dataSource.upsertHiddenKeySet(
                hiddenKeySet = any(),
            )
        } coAnswers {
            hiddenKeySetFlow.value = firstArg()
        }
    }
