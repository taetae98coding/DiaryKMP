package io.github.taetae98coding.diary.data.weather.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CurrentWeatherRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = fixtureMonkey.giveMeOne<CurrentWeatherRemoteEntity>()

            remote.toDomain() shouldBe
                Weather(
                    dateTime = remote.dateTime,
                    conditionList = remote.weather.map { condition -> condition.toDomain() },
                    temperature = remote.main.toDomain(),
                )
        }
    })
