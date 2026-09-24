@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.holiday.datasource

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@Single
internal class HolidayDirtyDataSource {
    private val cleanSet = AtomicReference(emptySet<Pair<HolidayCountry, Int>>())

    fun isDirty(
        country: HolidayCountry,
        year: Int,
    ): Boolean = country to year !in cleanSet.load()

    fun clean(
        country: HolidayCountry,
        year: Int,
    ) {
        cleanSet.update { set -> set + (country to year) }
    }
}
