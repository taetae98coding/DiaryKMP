package io.github.taetae98coding.diary.core.network.api.fcm.datasource

import io.github.taetae98coding.diary.core.network.api.fcm.entity.FcmTokenRemoteEntity

public interface FcmTokenRemoteDataSource {
    public suspend fun submit(fcmToken: FcmTokenRemoteEntity)
}
