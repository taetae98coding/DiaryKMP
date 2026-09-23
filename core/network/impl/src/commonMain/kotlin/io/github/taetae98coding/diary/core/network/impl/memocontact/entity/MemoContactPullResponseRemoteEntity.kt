package io.github.taetae98coding.diary.core.network.impl.memocontact.entity

import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoContactPullResponseRemoteEntity(
    @SerialName("memoContactList") val memoContactList: List<MemoContactPullRemoteEntity>,
)
