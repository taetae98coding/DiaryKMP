package io.github.taetae98coding.diary.core.network.impl.memoplace.entity

import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoPlacePushRequestRemoteEntity(
    @SerialName("memoPlaceList") val memoPlaceList: List<MemoPlaceRemoteEntity>,
)
