package io.github.taetae98coding.diary.core.network.impl.memo.entity

import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoPullResponseRemoteEntity(
    @SerialName("memoList") val memoList: List<MemoPullRemoteEntity>,
)
