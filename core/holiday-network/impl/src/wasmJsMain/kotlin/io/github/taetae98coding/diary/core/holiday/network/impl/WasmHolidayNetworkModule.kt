package io.github.taetae98coding.diary.core.holiday.network.impl

import io.github.taetae98coding.diary.core.holiday.network.impl.di.HolidayHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmHolidayNetworkModule {
    @Single
    @HolidayHttpClientEngine
    internal fun providesHolidayHttpClientEngine(): HttpClientEngine = Js.create()
}
