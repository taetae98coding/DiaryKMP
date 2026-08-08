package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

private const val SUBSTITUTE_HOLIDAY_KEY = "대체공휴일"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidaySettingListTest :
    BehaviorSpec({
        Given("TC-SETTING-HOLIDAY-DOMAIN-001 공백의 위치와 종류만 다른 공휴일 이름들이 저장되어 있다") {
            val nameAndKeyList =
                listOf(
                    "대체 공휴일" to SUBSTITUTE_HOLIDAY_KEY,
                    " 대체공휴일 " to SUBSTITUTE_HOLIDAY_KEY,
                    "대체　공휴일" to SUBSTITUTE_HOLIDAY_KEY,
                    "대체\t공휴일" to SUBSTITUTE_HOLIDAY_KEY,
                )

            When("각 공휴일 이름의 key를 만든다") {
                Then("모든 이름은 공백이 제거된 같은 key가 된다") {
                    nameAndKeyList.forEach { (name, expectedKey) ->
                        name.toHolidayKey() shouldBe expectedKey
                    }
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-003 같은 key의 공휴일들이 서로 다른 시작일로 저장되어 있다") {
            val olderName = "대체 공휴일"
            val latestName = "대체공휴일 "
            val holidayList =
                listOf(
                    holiday(name = latestName, start = LocalDate(year = 2026, month = 5, day = 6)),
                    holiday(name = olderName, start = LocalDate(year = 2025, month = 5, day = 6)),
                )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("같은 key는 최근 공휴일의 원래 이름으로 하나만 제공된다") {
                    settingList.size shouldBe 1
                    settingList.single().key shouldBe SUBSTITUTE_HOLIDAY_KEY
                    settingList.single().name shouldBe latestName
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-004 같은 key와 같은 최근 시작일을 가진 표기가 여러 개 저장되어 있다") {
            val recentStart = LocalDate(year = 2026, month = 5, day = 6)
            val recentNameList = listOf("대체공휴일 ", "대체 공휴일")
            val expectedName = recentNameList.min()
            val holidayList =
                recentNameList.map { name -> holiday(name = name, start = recentStart) } +
                    holiday(
                        name = " 대체공휴일",
                        start = LocalDate(year = 2025, month = 5, day = 6),
                    )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("최근 시작일의 이름 중 오름차순 첫 표기를 사용한다") {
                    settingList.single().name shouldBe expectedName
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-005 같은 key에 쉬는 날과 쉬는 날이 아닌 공휴일이 함께 저장되어 있다") {
            val holidayList =
                listOf(
                    holiday(name = "대체 공휴일", isHoliday = false),
                    holiday(name = "대체공휴일", isHoliday = true),
                )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("하나라도 쉬는 날이면 해당 key를 쉬는 날로 제공한다") {
                    settingList.single().isHoliday shouldBe true
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-006 표시 이름이 서로 다른 공휴일들이 순서 없이 저장되어 있다") {
            val holidayList =
                listOf(
                    holiday(name = "한글날"),
                    holiday(name = "Alpha"),
                    holiday(name = "광복절"),
                    holiday(name = "개천절"),
                )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("표시 이름과 key 오름차순으로 정렬된다") {
                    settingList.map { setting -> setting.key } shouldContainExactly
                        listOf("Alpha", "개천절", "광복절", "한글날")
                    settingList shouldContainExactly
                        settingList.sortedWith(
                            compareBy<HolidaySetting> { setting -> setting.name }
                                .thenBy { setting -> setting.key },
                        )
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-007 서로 다른 key 중 일부만 숨김으로 저장되어 있다") {
            val visibleNameList = listOf("개천절", "한글날")
            val hiddenKey = "광복절"
            val holidayList =
                (visibleNameList + hiddenKey).map { name ->
                    holiday(name = name)
                }

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = setOf(hiddenKey))

                Then("숨긴 key만 선택 해제 상태이고 나머지는 선택 상태다") {
                    settingList.associate { setting -> setting.key to setting.isVisible } shouldBe
                        mapOf(
                            "개천절" to true,
                            hiddenKey to false,
                            "한글날" to true,
                        )
                }
            }
        }
    })

private fun holiday(
    name: String,
    isHoliday: Boolean = fixtureMonkey.giveMeOne(),
    start: LocalDate = randomDate(),
): Holiday =
    Holiday(
        name = name,
        isHoliday = isHoliday,
        dateRange = start..start,
    )

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
