package io.github.taetae98coding.diary.data.lunar.cache

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.data.core.cache.FetchedKeySet

@Inject
@SingleIn(AppScope::class)
internal class LunarFetchedYearSet {
    private val keySet = FetchedKeySet<Int>()

    suspend fun <T> fetchOnce(
        key: Int,
        cached: suspend () -> T,
        fetch: suspend () -> T,
    ): T = keySet.fetchOnce(key = key, cached = cached, fetch = fetch)
}
