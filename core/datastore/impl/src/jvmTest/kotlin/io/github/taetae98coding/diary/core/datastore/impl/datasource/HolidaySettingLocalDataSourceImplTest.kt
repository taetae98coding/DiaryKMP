package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.datastore.impl.HolidaySettingData
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private const val MIDSUMMER_DAY_KEY = "초복"
private const val SECOND_MIDSUMMER_DAY_KEY = "중복"
private const val OTHER_HOLIDAY_KEY = "다른공휴일"

class HolidaySettingLocalDataSourceImplTest :
    FunSpec({
        test("보관된 것이 없으면 빈 집합을 제공한다") {
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(HolidaySettingData())))

            dataSource.getHiddenKeySet().first() shouldBe emptySet()
        }

        test("보관된 설정이 바뀌면 바뀐 숨긴 key를 이어서 제공한다") {
            val settingFlow = MutableStateFlow(HolidaySettingData())
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = mockDataStore(settingFlow))

            dataSource.getHiddenKeySet().test {
                awaitItem() shouldBe emptySet()

                settingFlow.value = HolidaySettingData(hiddenKeySet = setOf(MIDSUMMER_DAY_KEY))

                awaitItem() shouldBe setOf(MIDSUMMER_DAY_KEY)
            }
        }

        test("숨긴 key 하나를 DataStore의 한 번의 갱신으로 추가한다") {
            val initialSetting =
                HolidaySettingData(
                    hiddenKeySet = setOf(MIDSUMMER_DAY_KEY),
                )
            val settingFlow = MutableStateFlow(initialSetting)
            val dataStore = mockDataStore(settingFlow)
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.addHiddenKey(key = SECOND_MIDSUMMER_DAY_KEY)

            settingFlow.value shouldBe
                HolidaySettingData(
                    hiddenKeySet = setOf(MIDSUMMER_DAY_KEY, SECOND_MIDSUMMER_DAY_KEY),
                )
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("이미 숨긴 key를 추가하면 기존 설정 인스턴스를 유지하고 새 값을 제공하지 않는다") {
            val initialSetting = HolidaySettingData(hiddenKeySet = setOf(MIDSUMMER_DAY_KEY))
            val settingFlow = MutableStateFlow(initialSetting)
            var updatedSetting: HolidaySettingData? = null
            val dataStore =
                mockDataStore(settingFlow) { _, updated ->
                    updatedSetting = updated
                }
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(MIDSUMMER_DAY_KEY)

                dataSource.addHiddenKey(key = MIDSUMMER_DAY_KEY)

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            updatedSetting shouldBeSameInstanceAs initialSetting
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨긴 key 하나를 DataStore의 한 번의 갱신으로 제거한다") {
            val initialSetting =
                HolidaySettingData(
                    hiddenKeySet =
                        setOf(
                            MIDSUMMER_DAY_KEY,
                            SECOND_MIDSUMMER_DAY_KEY,
                        ),
                )
            val settingFlow = MutableStateFlow(initialSetting)
            val dataStore = mockDataStore(settingFlow)
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.removeHiddenKey(key = MIDSUMMER_DAY_KEY)

            settingFlow.value shouldBe
                HolidaySettingData(
                    hiddenKeySet = setOf(SECOND_MIDSUMMER_DAY_KEY),
                )
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨기지 않은 key를 제거하면 기존 설정 인스턴스를 유지한다") {
            val initialSetting = HolidaySettingData(hiddenKeySet = setOf(MIDSUMMER_DAY_KEY))
            val settingFlow = MutableStateFlow(initialSetting)
            var updatedSetting: HolidaySettingData? = null
            val dataStore =
                mockDataStore(settingFlow) { _, updated ->
                    updatedSetting = updated
                }
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.removeHiddenKey(key = SECOND_MIDSUMMER_DAY_KEY)

            updatedSetting shouldBeSameInstanceAs initialSetting
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨긴 key 집합으로 기존 집합을 DataStore의 한 번의 갱신으로 교체한다") {
            val initialSetting =
                HolidaySettingData(
                    hiddenKeySet =
                        setOf(
                            MIDSUMMER_DAY_KEY,
                            OTHER_HOLIDAY_KEY,
                        ),
                )
            val settingFlow = MutableStateFlow(initialSetting)
            val dataStore = mockDataStore(settingFlow)
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.submitHiddenKeySet(
                hiddenKeySet =
                    setOf(
                        SECOND_MIDSUMMER_DAY_KEY,
                        OTHER_HOLIDAY_KEY,
                    ),
            )

            settingFlow.value shouldBe
                HolidaySettingData(
                    hiddenKeySet =
                        setOf(
                            SECOND_MIDSUMMER_DAY_KEY,
                            OTHER_HOLIDAY_KEY,
                        ),
                )
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("빈 숨긴 key 집합을 제출하면 기존 집합을 모두 제거한다") {
            val settingFlow =
                MutableStateFlow(
                    HolidaySettingData(
                        hiddenKeySet = setOf(MIDSUMMER_DAY_KEY),
                    ),
                )
            val dataStore = mockDataStore(settingFlow)
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.submitHiddenKeySet(hiddenKeySet = emptySet())

            settingFlow.value shouldBe HolidaySettingData()
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("제출한 숨긴 key 집합이 같으면 기존 설정 인스턴스를 유지하고 새 값을 제공하지 않는다") {
            val initialSetting =
                HolidaySettingData(
                    hiddenKeySet =
                        setOf(
                            SECOND_MIDSUMMER_DAY_KEY,
                            OTHER_HOLIDAY_KEY,
                        ),
                )
            val settingFlow = MutableStateFlow(initialSetting)
            var updatedSetting: HolidaySettingData? = null
            val dataStore =
                mockDataStore(settingFlow) { _, updated ->
                    updatedSetting = updated
                }
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            dataSource.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(SECOND_MIDSUMMER_DAY_KEY, OTHER_HOLIDAY_KEY)

                dataSource.submitHiddenKeySet(
                    hiddenKeySet =
                        setOf(
                            SECOND_MIDSUMMER_DAY_KEY,
                            OTHER_HOLIDAY_KEY,
                        ),
                )

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            updatedSetting shouldBeSameInstanceAs initialSetting
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨긴 key 추가가 실패하면 같은 오류를 전파한다") {
            val failure = IllegalStateException("add failure")
            val settingFlow = MutableStateFlow(HolidaySettingData())
            val dataStore = mockk<DataStore<HolidaySettingData>>()
            every { dataStore.data } returns settingFlow
            coEvery { dataStore.updateData(any()) } throws failure
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            val actual =
                shouldThrowExactly<IllegalStateException> {
                    dataSource.addHiddenKey(key = MIDSUMMER_DAY_KEY)
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨긴 key 제거가 실패하면 같은 오류를 전파한다") {
            val failure = IllegalStateException("remove failure")
            val settingFlow = MutableStateFlow(HolidaySettingData())
            val dataStore = mockk<DataStore<HolidaySettingData>>()
            every { dataStore.data } returns settingFlow
            coEvery { dataStore.updateData(any()) } throws failure
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            val actual =
                shouldThrowExactly<IllegalStateException> {
                    dataSource.removeHiddenKey(key = MIDSUMMER_DAY_KEY)
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        test("숨긴 key 집합 제출이 실패하면 같은 오류를 전파한다") {
            val failure = IllegalStateException("submit failure")
            val settingFlow = MutableStateFlow(HolidaySettingData())
            val dataStore = mockk<DataStore<HolidaySettingData>>()
            every { dataStore.data } returns settingFlow
            coEvery { dataStore.updateData(any()) } throws failure
            val dataSource = HolidaySettingLocalDataSourceImpl(dataStore = dataStore)

            val actual =
                shouldThrowExactly<IllegalStateException> {
                    dataSource.submitHiddenKeySet(
                        hiddenKeySet = setOf(MIDSUMMER_DAY_KEY),
                    )
                }

            actual shouldBeSameInstanceAs failure
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }
    })

private fun mockDataStore(
    settingFlow: MutableStateFlow<HolidaySettingData>,
    onUpdate: (previous: HolidaySettingData, updated: HolidaySettingData) -> Unit = { _, _ -> },
): DataStore<HolidaySettingData> =
    mockk<DataStore<HolidaySettingData>>().also { dataStore ->
        every { dataStore.data } returns settingFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val previous = settingFlow.value
            val updated = firstArg<suspend (HolidaySettingData) -> HolidaySettingData>().invoke(previous)

            onUpdate(previous, updated)
            settingFlow.value = updated
            updated
        }
    }
