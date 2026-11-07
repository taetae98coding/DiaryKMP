package io.github.taetae98coding.diary.data.weather.mapper

import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.testing.weather.weatherConditionRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherConditionRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WeatherConditionRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain uses day icon image url") {
            mapOf(
                "01d" to "01d",
                "10n" to "10d",
            ).forEach { (remoteIcon, expectedIcon) ->
                val remote =
                    Arb
                        .weatherConditionRemoteEntity()
                        .next()
                        .copy(icon = remoteIcon)

                remote.toDomain() shouldBe
                    WeatherCondition(
                        description = remote.description,
                        imageUrl = "https://openweathermap.org/img/wn/$expectedIcon.png",
                    )
            }
        }
    })
