package io.github.taetae98coding.diary.domain.account.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.core.invoke
import kotlinx.coroutines.flow.first

@Inject
public class RefreshUserDataUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val userDataRepository: UserDataRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        // 계정 확인에 실패하면 게스트일 때와 같이 다시 확인할 사용자 정보가 없으므로 요청하지 않고 끝낸다.
        val account = getAccountUseCase().first().getOrNull()

        if (account !is Account.User) return

        userDataRepository.refresh()
    }
}
