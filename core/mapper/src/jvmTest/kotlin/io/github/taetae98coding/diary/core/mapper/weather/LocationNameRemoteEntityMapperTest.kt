package io.github.taetae98coding.diary.core.mapper.weather

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LocationNameRemoteEntityMapperTest :
    FunSpec({
        test("한국어 이름이 있으면 한국어 이름을 사용한다") {
            val remote =
                fixtureMonkey
                    .giveMeOne<LocationNameRemoteEntity>()
                    .copy(name = "Seongnam-si", localNames = mapOf("en" to "Seongnam-si", "ko" to "성남시"))

            listOf(remote).toLocationName() shouldBe "성남시"
        }

        test("한국어 이름이 없으면 기본 이름을 사용한다") {
            val remote =
                fixtureMonkey
                    .giveMeOne<LocationNameRemoteEntity>()
                    .copy(name = "Paris", localNames = mapOf("en" to "Paris"))

            listOf(remote).toLocationName() shouldBe "Paris"
        }

        test("여러 지역이 오면 첫 번째 지역의 이름을 사용한다") {
            val first = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Seongnam-si", localNames = mapOf("ko" to "성남시"))
            val second = fixtureMonkey.giveMeOne<LocationNameRemoteEntity>().copy(name = "Yongin-si", localNames = mapOf("ko" to "용인시"))

            listOf(first, second).toLocationName() shouldBe "성남시"
        }

        test("빈 목록이면 지역명이 없다") {
            emptyList<LocationNameRemoteEntity>().toLocationName() shouldBe ""
        }
    })
