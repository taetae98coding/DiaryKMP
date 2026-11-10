package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.exceptions.RestException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.core.supabase.impl.mapper.toSupabaseSessionStatus
import io.github.taetae98coding.diary.core.supabase.impl.mapper.toSupabaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class SupabaseAuthImpl(
    private val client: SupabaseClient,
) : SupabaseAuth {
    override suspend fun importAuthToken(
        accessToken: String,
        refreshToken: String,
    ) {
        client.auth.importAuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            retrieveUser = true,
            autoRefresh = true,
        )
    }

    override fun getSessionStatusFlow(): Flow<SupabaseSessionStatus> =
        client.auth.sessionStatus
            .map { status -> status.toSupabaseSessionStatus() }

    // 인증 제공자는 사용자 정보를 갱신할 때 새 상태를 먼저 알리고 저장은 그 뒤에 하므로,
    // 알림을 받고 저장소를 다시 읽으면 갱신 직전 값을 읽는다. 알림이 담아 온 세션을 그대로 쓴다.
    override fun getUserFlow(): Flow<SupabaseUser?> =
        client.auth.sessionStatus
            .map { status ->
                (status as? SessionStatus.Authenticated)?.session?.user?.toSupabaseUser() ?: loadStoredUser()
            }

    override suspend fun retrieveUserForCurrentSession() {
        client.auth.retrieveUserForCurrentSession(updateSession = true)
    }

    // 인증 제공자는 앱이 화면에서 벗어나면 자동 갱신을 멈추고 화면에 다시 보일 때 저장된 세션으로 다시 켠다.
    // 여기서 자동 갱신을 켜 두면 화면에 다시 보일 때 다시 켜지 않고, 운영체제가 앱을 멈춘 동안 갱신 시점을 놓친 타이머를 그대로 쓴다.
    // 그래서 저장된 세션을 한 번만 갱신하고 자동 갱신 없이 설정한다.
    override suspend fun refreshStoppedSession() {
        val auth = client.auth
        if (auth.isAutoRefreshRunning) return

        auth.sessionManager.loadSessionOrNull()?.let { storedSession -> refreshStoredSession(storedSession) }
    }

    private suspend fun refreshStoredSession(storedSession: UserSession) {
        val auth = client.auth

        try {
            val refreshedSession = auth.refreshSession(refreshToken = storedSession.refreshToken)

            auth.importSession(
                session = refreshedSession,
                autoRefresh = false,
                source = SessionSource.Refresh(oldSession = storedSession),
            )
        } catch (exception: RestException) {
            // 인증 제공자의 자동 갱신과 같이 5xx는 일시적인 오류로, 나머지는 갱신 정보가 폐기된 것으로 보고 세션을 지운다.
            if (exception.statusCode >= SERVER_ERROR_STATUS_CODE) throw exception

            auth.clearSession()
        }
    }

    // 인증 제공자의 signOut은 범위와 관계없이 서버에 세션 폐기를 먼저 요청하고, 그 요청이 연결 오류로 실패하면
    // 기기의 세션을 지우지 않은 채 예외를 던진다. 그래서 signOut 대신 기기의 세션만 지운다.
    override suspend fun signOut() {
        client.auth.clearSession()
    }

    private suspend fun loadStoredUser(): SupabaseUser? =
        client.auth.sessionManager
            .loadSessionOrNull()
            ?.user
            ?.toSupabaseUser()

    private companion object {
        private const val SERVER_ERROR_STATUS_CODE = 500
    }
}
