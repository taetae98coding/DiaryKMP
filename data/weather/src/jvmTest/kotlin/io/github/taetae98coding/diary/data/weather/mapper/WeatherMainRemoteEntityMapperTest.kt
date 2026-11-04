package io.github.taetae98coding.diary.data.weather.mapper

import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.core.testing.weather.weatherMainRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherMainRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WeatherMainRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain") {
            val remote = Arb.weatherMainRemoteEntity().next()

            remote.toDomain() shouldBe
                WeatherTemperature(
                    current = remote.temp,
                    min = remote.tempMin,
                    max = remote.tempMax,
                )
        }
    })
