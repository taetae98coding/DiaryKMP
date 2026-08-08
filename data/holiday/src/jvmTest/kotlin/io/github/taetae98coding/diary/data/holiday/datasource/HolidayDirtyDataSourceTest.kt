package io.github.taetae98coding.diary.data.holiday.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayDirtyDataSourceTest :
    FunSpec({
        test("기록되지 않은 연도는 동기화가 필요하다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val dataSource = HolidayDirtyDataSource()

            dataSource.isDirty(year = year) shouldBe true
        }

        test("기록한 연도는 동기화가 필요하지 않다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val dataSource = HolidayDirtyDataSource()

            dataSource.clean(year = year)

            dataSource.isDirty(year = year) shouldBe false
        }

        test("한 연도를 기록해도 다른 연도는 동기화가 필요하다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
            val dataSource = HolidayDirtyDataSource()

            dataSource.clean(year = year)

            dataSource.isDirty(year = otherYear) shouldBe true
        }

        test("같은 연도를 여러 번 기록해도 동기화가 필요하지 않은 상태를 유지한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val dataSource = HolidayDirtyDataSource()

            repeat(3) { dataSource.clean(year = year) }

            dataSource.isDirty(year = year) shouldBe false
        }

        test("새로 만든 기록은 이전 기록을 이어받지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            HolidayDirtyDataSource().clean(year = year)

            HolidayDirtyDataSource().isDirty(year = year) shouldBe true
        }
    })
