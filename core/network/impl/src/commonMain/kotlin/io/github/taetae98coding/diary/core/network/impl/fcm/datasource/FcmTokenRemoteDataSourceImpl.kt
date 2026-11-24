package io.github.taetae98coding.diary.core.network.impl.fcm.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.network.api.fcm.datasource.FcmTokenRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.fcm.entity.FcmTokenRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke

@Inject
@ContributesBinding(AppScope::class)
internal class FcmTokenRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : FcmTokenRemoteDataSource {
    override suspend fun upsert(fcmToken: FcmTokenRemoteEntity) {
        supabaseFunction(function = SUBMIT_FCM_TOKEN_FUNCTION, body = fcmToken)
    }

    private companion object {
        const val SUBMIT_FCM_TOKEN_FUNCTION: String = "v1-fcm-token-submit"
    }
}
