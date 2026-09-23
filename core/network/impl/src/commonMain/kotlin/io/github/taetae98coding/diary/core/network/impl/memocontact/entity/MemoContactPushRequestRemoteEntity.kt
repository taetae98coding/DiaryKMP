package io.github.taetae98coding.diary.core.network.impl.memocontact.entity

import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoContactPushRequestRemoteEntity(
    @SerialName("memoContactList") val memoContactList: List<MemoContactRemoteEntity>,
)
