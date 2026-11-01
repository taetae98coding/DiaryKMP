package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.Session
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

@Factory
public class GetAccountUseCase internal constructor(
    private val sessionRepository: SessionRepository,
    private val userDataRepository: UserDataRepository,
) : FlowUseCase<Unit, Account>() {
    override fun execute(parameter: Unit): Flow<Result<Account>> =
        combine(
            sessionRepository.get(),
            userDataRepository.get(),
        ) { session, userData ->
            Result.success(toAccount(session, userData))
        }

    private fun toAccount(
        session: Session,
        userData: UserData?,
    ): Account =
        if (userData == null) {
            Account.Guest
        } else {
            Account.User(
                id = userData.id,
                profileImage = userData.profileImage,
                email = userData.email,
                isSessionValid = session is Session.Authenticated,
            )
        }
}
