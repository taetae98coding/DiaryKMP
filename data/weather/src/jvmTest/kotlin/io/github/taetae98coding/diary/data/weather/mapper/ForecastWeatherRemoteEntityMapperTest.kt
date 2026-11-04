package io.github.taetae98coding.diary.data.weather.mapper

import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.testing.weather.forecastWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastWeatherRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ForecastWeatherRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = Arb.forecastWeatherRemoteEntity().next()

            remote.toDomain() shouldBe
                Weather(
                    dateTime = remote.dateTime,
                    conditionList = remote.weather.map { condition -> condition.toDomain() },
                    temperature = remote.main.toDomain(),
                )
        }
    })
