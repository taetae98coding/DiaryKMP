package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CalendarColorTest :
    FunSpec({
        test("공휴일은 요일과 관계없이 일요일 및 공휴일 색상을 반환한다") {
            val colors = fixtureMonkey.giveMeCalendarColor()
            val defaultColor = fixtureMonkey.giveMeColor()

            DayOfWeek.entries.forEach { dayOfWeek ->
                colors.dayOfWeekColor(
                    dayOfWeek = dayOfWeek,
                    defaultColor = defaultColor,
                    isHoliday = true,
                ) shouldBe colors.sundayAndHolidayColor
            }
        }

        test("일요일은 일요일 및 공휴일 색상을 반환한다") {
            val colors = fixtureMonkey.giveMeCalendarColor()
            val defaultColor = fixtureMonkey.giveMeColor()

            colors.dayOfWeekColor(
                dayOfWeek = DayOfWeek.SUNDAY,
                defaultColor = defaultColor,
            ) shouldBe colors.sundayAndHolidayColor
        }

        test("토요일은 토요일 색상을 반환한다") {
            val colors = fixtureMonkey.giveMeCalendarColor()
            val defaultColor = fixtureMonkey.giveMeColor()

            colors.dayOfWeekColor(
                dayOfWeek = DayOfWeek.SATURDAY,
                defaultColor = defaultColor,
            ) shouldBe colors.saturdayColor
        }

        test("평일은 전달된 기본 내용 색상을 반환한다") {
            val colors = fixtureMonkey.giveMeCalendarColor()
            val defaultColor = fixtureMonkey.giveMeColor()
            val weekdays = DayOfWeek.entries - DayOfWeek.SUNDAY - DayOfWeek.SATURDAY

            weekdays.forEach { dayOfWeek ->
                colors.dayOfWeekColor(
                    dayOfWeek = dayOfWeek,
                    defaultColor = defaultColor,
                ) shouldBe defaultColor
            }
        }
    })

private fun FixtureMonkey.giveMeCalendarColor(): CalendarColor =
    CalendarColor(
        sundayAndHolidayColor = giveMeColor(),
        saturdayColor = giveMeColor(),
    )

private fun FixtureMonkey.giveMeColor(): Color = Color(color = giveMeOne<Int>())
