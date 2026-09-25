package io.github.taetae98coding.diary.core.integrity.impl

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import com.google.firebase.FirebaseApp
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityTokenProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

// 토큰 제공자 준비는 수 초가 걸리므로 한 번 준비한 제공자를 프로세스 동안 재사용한다.
@Single
internal class AndroidPlayIntegrityTokenProvider(
    private val context: Context,
) : PlayIntegrityTokenProvider {
    private val mutex = Mutex()
    private var tokenProvider: StandardIntegrityTokenProvider? = null

    override suspend fun getToken(requestHash: String): PlayIntegrityToken {
        val request = StandardIntegrityTokenRequest.builder().setRequestHash(requestHash).build()
        val token =
            try {
                preparedTokenProvider().request(request).await().token()
            } catch (exception: StandardIntegrityException) {
                // 준비한 제공자는 시간이 지나면 무효가 되므로 버리고, 다음 확인에서 다시 준비한다.
                if (exception.errorCode == StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID) {
                    mutex.withLock { tokenProvider = null }
                }
                throw exception
            }

        return PlayIntegrityToken(token = token, packageName = context.packageName)
    }

    private suspend fun preparedTokenProvider(): StandardIntegrityTokenProvider =
        mutex.withLock {
            tokenProvider ?: IntegrityManagerFactory
                .createStandard(context)
                .prepareIntegrityToken(PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(cloudProjectNumber()).build())
                .await()
                .also { prepared -> tokenProvider = prepared }
        }

    // google-services.json의 project_number가 Firebase 옵션의 GCM sender ID로 들어온다.
    // Play Console에서 앱에 연결하는 Cloud 프로젝트도 이 Firebase 프로젝트다.
    private fun cloudProjectNumber(): Long = requireNotNull(FirebaseApp.getInstance().options.gcmSenderId).toLong()
}
