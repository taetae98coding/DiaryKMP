package io.github.taetae98coding.diary.core.mapper.weather

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherMainRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WeatherMainRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = fixtureMonkey.giveMeOne<WeatherMainRemoteEntity>()

            remote.toDomain() shouldBe
                WeatherTemperature(
                    current = remote.temp,
                    min = remote.tempMin,
                    max = remote.tempMax,
                )
        }
    })
