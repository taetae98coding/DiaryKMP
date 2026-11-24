package io.github.taetae98coding.diary.data.holiday.cache

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.data.core.cache.FetchedKeySet

@Inject
@SingleIn(AppScope::class)
internal class HolidayFetchedKeySet {
    private val keySet = FetchedKeySet<Pair<HolidayCountry, Int>>()

    suspend fun <T> fetchOnce(
        key: Pair<HolidayCountry, Int>,
        cached: suspend () -> T,
        fetch: suspend () -> T,
    ): T = keySet.fetchOnce(key = key, cached = cached, fetch = fetch)
}
