package io.github.taetae98coding.diary.data.lunar.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LunarDirtyDataSourceTest :
    FunSpec({
        test("기록되지 않은 연도는 동기화가 필요하다") {
            val year = fixtureMonkey.giveMeOne<Int>()

            LunarDirtyDataSource().isDirty(year = year) shouldBe true
        }

        test("기록한 연도는 동기화가 필요하지 않다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val dataSource = LunarDirtyDataSource()

            dataSource.clean(year = year)

            dataSource.isDirty(year = year) shouldBe false
        }

        test("한 연도를 기록해도 다른 연도는 동기화가 필요하다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val otherYear = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != year }
            val dataSource = LunarDirtyDataSource()

            dataSource.clean(year = year)

            dataSource.isDirty(year = otherYear) shouldBe true
        }

        test("새로 만든 기록은 이전 기록을 이어받지 않는다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            LunarDirtyDataSource().clean(year = year)

            LunarDirtyDataSource().isDirty(year = year) shouldBe true
        }
    })
