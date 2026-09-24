package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import io.github.taetae98coding.diary.core.network.api.fcm.datasource.FcmTokenRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.fcm.entity.FcmTokenRemoteEntity
import io.github.taetae98coding.diary.domain.account.repository.FcmTokenRepository
import io.github.taetae98coding.diary.library.locale.DeviceLocale
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

@Factory
internal class FcmTokenRepositoryImpl(
    private val fcmTokenProvider: FcmTokenProvider,
    private val fcmTokenRemoteDataSource: FcmTokenRemoteDataSource,
) : FcmTokenRepository {
    override suspend fun upsert() {
        val token = fcmTokenProvider.getToken() ?: return

        fcmTokenRemoteDataSource.upsert(
            fcmToken =
                FcmTokenRemoteEntity(
                    token = token,
                    timeZone = TimeZone.currentSystemDefault().id,
                    language = DeviceLocale.currentLanguageTag(),
                ),
        )
    }

    override suspend fun delete() {
        val token = fcmTokenProvider.getToken() ?: return

        fcmTokenRemoteDataSource.upsert(fcmToken = FcmTokenRemoteEntity(token = token))
    }
}
