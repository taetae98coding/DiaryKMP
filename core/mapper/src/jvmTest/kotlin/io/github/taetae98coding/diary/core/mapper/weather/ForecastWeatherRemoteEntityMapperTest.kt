package io.github.taetae98coding.diary.core.mapper.weather

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastWeatherRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ForecastWeatherRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = fixtureMonkey.giveMeOne<ForecastWeatherRemoteEntity>()

            remote.toDomain() shouldBe
                Weather(
                    dateTime = remote.dateTime,
                    conditionList = remote.weather.map { condition -> condition.toDomain() },
                    temperature = remote.main.toDomain(),
                )
        }
    })
