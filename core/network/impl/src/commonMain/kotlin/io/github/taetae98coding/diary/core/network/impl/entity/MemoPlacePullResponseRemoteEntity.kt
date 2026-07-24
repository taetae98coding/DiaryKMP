package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlacePullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoPlacePullResponseRemoteEntity(
    @SerialName("memoPlaceList") val memoPlaceList: List<MemoPlacePullRemoteEntity>,
)
