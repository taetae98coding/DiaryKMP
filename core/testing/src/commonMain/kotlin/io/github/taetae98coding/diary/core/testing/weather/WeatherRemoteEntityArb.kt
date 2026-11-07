package io.github.taetae98coding.diary.core.testing.weather

import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherConditionRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherMainRemoteEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.string

public fun Arb.Companion.currentWeatherRemoteEntity(): Arb<CurrentWeatherRemoteEntity> =
    arbitrary {
        CurrentWeatherRemoteEntity(
            weather = Arb.list(Arb.weatherConditionRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind(),
            main = Arb.weatherMainRemoteEntity().bind(),
            dateTime = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.forecastRemoteEntity(): Arb<ForecastRemoteEntity> =
    arbitrary {
        ForecastRemoteEntity(
            list = Arb.list(Arb.forecastWeatherRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.forecastWeatherRemoteEntity(): Arb<ForecastWeatherRemoteEntity> =
    arbitrary {
        ForecastWeatherRemoteEntity(
            dateTime = Arb.secondPrecisionInstant().bind(),
            main = Arb.weatherMainRemoteEntity().bind(),
            weather = Arb.list(Arb.weatherConditionRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.locationNameRemoteEntity(): Arb<LocationNameRemoteEntity> =
    arbitrary {
        LocationNameRemoteEntity(
            name = Arb.string().bind(),
            localNames = Arb.list(Arb.pair(Arb.string(), Arb.string()), 0..MAX_COLLECTION_SIZE).map { it.toMap() }.bind(),
        )
    }

public fun Arb.Companion.weatherConditionRemoteEntity(): Arb<WeatherConditionRemoteEntity> =
    arbitrary {
        WeatherConditionRemoteEntity(
            description = Arb.string().bind(),
            icon = Arb.string().bind(),
        )
    }

public fun Arb.Companion.weatherMainRemoteEntity(): Arb<WeatherMainRemoteEntity> =
    arbitrary {
        WeatherMainRemoteEntity(
            temp = Arb.numericDouble().bind(),
            tempMin = Arb.numericDouble().bind(),
            tempMax = Arb.numericDouble().bind(),
        )
    }
