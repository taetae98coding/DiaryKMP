package io.github.taetae98coding.diary.data.core.cache

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchedKeySetTest :
    FunSpec({
        test("기록되지 않은 키는 받아 오지 않은 것이다") {
            TestFetchedKeySet().isFetched(key = fixtureMonkey.giveMeOne()) shouldBe false
        }

        test("기록한 키는 받아 온 것이다") {
            val key = fixtureMonkey.giveMeOne<Int>()
            val keySet = TestFetchedKeySet()

            keySet.add(key = key)

            keySet.isFetched(key = key) shouldBe true
        }

        test("한 키를 기록해도 다른 키는 받아 오지 않은 것이다") {
            val key = fixtureMonkey.giveMeOne<Int>()
            val otherKey = generateSequence { fixtureMonkey.giveMeOne<Int>() }.first { candidate -> candidate != key }
            val keySet = TestFetchedKeySet()

            keySet.add(key = key)

            keySet.isFetched(key = otherKey) shouldBe false
        }

        test("같은 키를 여러 번 기록해도 받아 온 상태를 유지한다") {
            val key = fixtureMonkey.giveMeOne<Int>()
            val keySet = TestFetchedKeySet()

            repeat(3) { keySet.add(key = key) }

            keySet.isFetched(key = key) shouldBe true
        }

        test("새로 만든 기록은 이전 기록을 이어받지 않는다") {
            val key = fixtureMonkey.giveMeOne<Int>()
            TestFetchedKeySet().add(key = key)

            TestFetchedKeySet().isFetched(key = key) shouldBe false
        }
    })

private class TestFetchedKeySet : FetchedKeySet<Int>()
