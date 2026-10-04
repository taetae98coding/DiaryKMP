package io.github.taetae98coding.diary.data.holiday.cache

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.data.core.cache.FetchedKeySet
import org.koin.core.annotation.Single

@Single
internal class HolidayFetchedKeySet : FetchedKeySet<Pair<HolidayCountry, Int>>()
