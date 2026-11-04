package io.github.taetae98coding.diary.core.testing.weather

import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.string

public fun Arb.Companion.weather(): Arb<Weather> =
    arbitrary {
        Weather(
            dateTime = Arb.secondPrecisionInstant().bind(),
            conditionList = Arb.list(Arb.weatherCondition(), 0..MAX_COLLECTION_SIZE).bind(),
            temperature = Arb.weatherTemperature().bind(),
        )
    }

public fun Arb.Companion.weatherCondition(): Arb<WeatherCondition> =
    arbitrary {
        WeatherCondition(
            description = Arb.string().bind(),
            imageUrl = Arb.string().bind(),
        )
    }

public fun Arb.Companion.weatherTemperature(): Arb<WeatherTemperature> =
    arbitrary {
        WeatherTemperature(
            current = Arb.numericDouble().bind(),
            min = Arb.numericDouble().bind(),
            max = Arb.numericDouble().bind(),
        )
    }
