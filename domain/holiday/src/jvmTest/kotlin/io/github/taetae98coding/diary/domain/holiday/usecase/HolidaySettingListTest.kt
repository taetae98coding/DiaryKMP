package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidaySettingListTest :
    BehaviorSpec({
        Given("TC-SETTING-HOLIDAY-DOMAIN-014 같은 이름의 공휴일이 서로 다른 연도에 저장되어 있다") {
            val name = "대체공휴일(어린이날)"
            val holidayList =
                listOf(
                    holiday(name = name, start = LocalDate(year = 2026, month = 5, day = 6)),
                    holiday(name = name, start = LocalDate(year = 2025, month = 5, day = 6)),
                )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("그 이름의 항목은 하나만 제공되고 표시 이름은 저장된 이름 그대로다") {
                    settingList.size shouldBe 1
                    settingList.single().name shouldBe name
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-005 같은 key에 쉬는 날과 쉬는 날이 아닌 공휴일이 함께 저장되어 있다") {
            val name = "대체공휴일"
            val holidayList =
                listOf(
                    holiday(name = name, isHoliday = false),
                    holiday(name = name, isHoliday = true),
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

                Then("표시 이름 오름차순으로 정렬된다") {
                    settingList.map { setting -> setting.name } shouldContainExactly
                        listOf("Alpha", "개천절", "광복절", "한글날")
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
                    settingList.associate { setting -> setting.name to setting.isVisible } shouldBe
                        mapOf(
                            "개천절" to true,
                            hiddenKey to false,
                            "한글날" to true,
                        )
                }
            }
        }

        Given("공백만 다른 이름의 공휴일이 저장되어 있다") {
            val holidayList =
                listOf(
                    holiday(name = "대체 공휴일"),
                    holiday(name = "대체공휴일"),
                )

            When("설정 화면에 표시할 공휴일 항목을 구성한다") {
                val settingList = holidayList.toHolidaySettingList(hiddenKeySet = emptySet())

                Then("이름을 가공하지 않고 서로 다른 항목으로 제공한다") {
                    settingList.map { setting -> setting.name } shouldContainExactly listOf("대체 공휴일", "대체공휴일")
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
