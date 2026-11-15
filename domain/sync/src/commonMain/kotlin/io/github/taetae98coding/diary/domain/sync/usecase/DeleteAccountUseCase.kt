package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.domain.account.repository.AccountRepository
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncDataRepository
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

@Factory
public class DeleteAccountUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRepository: AccountRepository,
    private val accountSyncDataRepository: AccountSyncDataRepository,
    private val accountSyncTimeRepository: AccountSyncTimeRepository,
    private val signOutUseCase: SignOutUseCase,
) : UseCase<AppleAuthorization?, Unit>() {
    override suspend fun execute(parameter: AppleAuthorization?) {
        val account = getAccountUseCase.requireAccount()
        if (account !is Account.User || !account.isSessionValid) {
            throw IllegalStateException("account session is not valid")
        }

        accountRepository.delete(appleAuthorization = parameter)
        runIgnoringFailure { accountSyncDataRepository.delete(account = account) }
        runIgnoringFailure { accountSyncTimeRepository.delete(account = account) }
        signOutUseCase()
    }

    // 서버의 계정은 이미 지워져 되돌릴 수 없으므로, 기기 데이터를 지우지 못해도 나머지 정리와 로그아웃을 이어 간다.
    private suspend fun runIgnoringFailure(block: suspend () -> Unit) {
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
        }
    }
}
