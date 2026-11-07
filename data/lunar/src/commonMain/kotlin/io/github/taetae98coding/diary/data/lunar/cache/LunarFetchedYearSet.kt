package io.github.taetae98coding.diary.data.lunar.cache

import io.github.taetae98coding.diary.data.core.cache.FetchedKeySet
import org.koin.core.annotation.Single

@Single
internal class LunarFetchedYearSet {
    private val keySet = FetchedKeySet<Int>()

    suspend fun <T> fetchOnce(
        key: Int,
        cached: suspend () -> T,
        fetch: suspend () -> T,
    ): T = keySet.fetchOnce(key = key, cached = cached, fetch = fetch)
}
