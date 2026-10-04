@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.core.cache

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

// 프로세스가 살아 있는 동안 원격에서 이미 받아 온 키를 기억한다. 저장소마다 따로 기억해야 하므로 모듈마다 하위 클래스를 싱글턴으로 둔다.
public abstract class FetchedKeySet<K> {
    private val fetchedKeySet = AtomicReference(emptySet<K>())

    public fun isFetched(key: K): Boolean = key in fetchedKeySet.load()

    public fun add(key: K) {
        fetchedKeySet.update { set -> set + key }
    }
}
