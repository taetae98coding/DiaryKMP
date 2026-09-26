package io.github.taetae98coding.diary.core.network.impl.qr.datasource

import io.github.taetae98coding.diary.core.network.api.qr.datasource.QrRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.qr.entity.QrPullResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.qr.entity.QrPushRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import org.koin.core.annotation.Factory

@Factory
internal class QrRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : QrRemoteDataSource {
    override suspend fun push(qrList: List<QrRemoteEntity>) {
        supabaseFunction(
            function = PUSH_QR_FUNCTION,
            body = QrPushRequestRemoteEntity(qrList = qrList),
        )
    }

    override suspend fun pull(usn: Long): List<QrPullRemoteEntity> =
        supabaseFunction(
            function = PULL_QR_FUNCTION,
            body = PullRequestRemoteEntity(usn = usn),
        ).body<QrPullResponseRemoteEntity>().qrList

    private companion object {
        const val PUSH_QR_FUNCTION: String = "v1-sync-push-qr"
        const val PULL_QR_FUNCTION: String = "v1-sync-pull-qr"
    }
}
