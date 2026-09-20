package io.github.taetae98coding.diary.data.weather.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherConditionRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class WeatherConditionRemoteEntityMapperTest :
    FunSpec({
        test("remote to domain uses day icon image url") {
            mapOf(
                "01d" to "01d",
                "10n" to "10d",
            ).forEach { (remoteIcon, expectedIcon) ->
                val remote =
                    fixtureMonkey
                        .giveMeOne<WeatherConditionRemoteEntity>()
                        .copy(icon = remoteIcon)

                remote.toDomain() shouldBe
                    WeatherCondition(
                        description = remote.description,
                        imageUrl = "https://openweathermap.org/img/wn/$expectedIcon.png",
                    )
            }
        }
    })
