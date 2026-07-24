package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoPushRequestRemoteEntity(
    @SerialName("memoList") val memoList: List<MemoRemoteEntity>,
)
