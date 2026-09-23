package io.github.taetae98coding.diary.core.network.impl.memotag.entity

import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoTagPushRequestRemoteEntity(
    @SerialName("memoTagList") val memoTagList: List<MemoTagRemoteEntity>,
)
