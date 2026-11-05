package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.logCrashlyticsFailure
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

@Factory
public class SignInWithGoogleUseCase internal constructor(
    private val sessionRepository: SessionRepository,
    private val loginProviderRepository: LoginProviderRepository,
) : UseCase<GoogleCredential, Unit>() {
    override suspend fun execute(parameter: GoogleCredential) {
        sessionRepository.create(credential = parameter)
        // 세션 설정까지 끝난 로그인은 수단 기록에 실패해도 성공으로 처리한다.
        runCatching { loginProviderRepository.set(provider = LoginProvider.GOOGLE) }
            .onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                DiaryLogger.log(log = ConsoleLog(tag = "SignInWithGoogleUseCase", message = "로그인 수단 기록 실패", throwable = throwable))
            }
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.logCrashlyticsFailure(source = this, throwable = throwable)
    }
}
