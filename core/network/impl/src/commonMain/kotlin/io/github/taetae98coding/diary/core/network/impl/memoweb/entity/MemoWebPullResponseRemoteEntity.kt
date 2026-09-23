package io.github.taetae98coding.diary.core.network.impl.memoweb.entity

import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoWebPullResponseRemoteEntity(
    @SerialName("memoWebList") val memoWebList: List<MemoWebPullRemoteEntity>,
)
