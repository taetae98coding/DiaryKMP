@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.core.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.concurrent.atomics.updateAndFetch

// 프로세스가 살아 있는 동안 원격에서 이미 받아 온 키를 기억한다. 저장소마다 따로 기억해야 하므로 모듈마다 하위 클래스를 싱글턴으로 둔다.
public abstract class FetchedKeySet<K> {
    private val fetchedKeySet = AtomicReference(emptySet<K>())
    private val mutexMap = AtomicReference(emptyMap<K, Mutex>())

    // 키마다 잠가 같은 키의 요청이 앞선 요청을 기다린 뒤 기록을 다시 확인하게 한다.
    public suspend fun <T> fetchOnce(
        key: K,
        cached: suspend () -> T,
        fetch: suspend () -> T,
    ): T =
        mutex(key = key).withLock {
            if (key in fetchedKeySet.load()) return@withLock cached()

            fetch().also { fetchedKeySet.update { set -> set + key } }
        }

    private fun mutex(key: K): Mutex =
        mutexMap
            .updateAndFetch { map -> if (key in map) map else map + (key to Mutex()) }
            .getValue(key)
}
