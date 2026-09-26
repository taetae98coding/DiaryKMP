package io.github.taetae98coding.diary.core.network.api.qr.datasource

import io.github.taetae98coding.diary.core.network.api.qr.entity.QrPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity

public interface QrRemoteDataSource {
    public suspend fun push(qrList: List<QrRemoteEntity>)

    public suspend fun pull(usn: Long): List<QrPullRemoteEntity>
}
