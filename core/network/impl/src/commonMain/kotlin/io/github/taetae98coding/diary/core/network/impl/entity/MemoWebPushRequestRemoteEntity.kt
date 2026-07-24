package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoWebPushRequestRemoteEntity(
    @SerialName("memoWebList") val memoWebList: List<MemoWebRemoteEntity>,
)
