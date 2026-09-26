package io.github.taetae98coding.diary.core.network.impl.qr.entity

import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QrPushRequestRemoteEntity(
    @SerialName("qrList") val qrList: List<QrRemoteEntity>,
)
