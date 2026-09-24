@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.lunar.datasource

import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

@Single
internal class LunarDirtyDataSource {
    private val cleanYearSet = AtomicReference(emptySet<Int>())

    fun isDirty(year: Int): Boolean = year !in cleanYearSet.load()

    fun clean(year: Int) {
        cleanYearSet.update { set -> set + year }
    }
}
