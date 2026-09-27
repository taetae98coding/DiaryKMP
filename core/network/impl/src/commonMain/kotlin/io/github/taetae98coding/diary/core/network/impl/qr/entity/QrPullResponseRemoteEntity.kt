package io.github.taetae98coding.diary.core.network.impl.qr.entity

import io.github.taetae98coding.diary.core.network.api.qr.entity.QrPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QrPullResponseRemoteEntity(
    @SerialName("qrList") val qrList: List<QrPullRemoteEntity>,
)
