@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.holiday.datasource

import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@Single
internal class HolidayDirtyDataSource {
    private val cleanYearSet = AtomicReference(emptySet<Int>())

    fun isDirty(year: Int): Boolean = year !in cleanYearSet.load()

    fun clean(year: Int) {
        cleanYearSet.update { yearSet -> yearSet + year }
    }
}
