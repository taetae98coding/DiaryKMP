package io.github.taetae98coding.diary.domain.qr.usecase

import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindQrUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountQrRepository: AccountQrRepository,
) : FlowUseCase<Uuid, Qr?>() {
    override fun execute(parameter: Uuid): Flow<Result<Qr?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountQrRepository
                .find(
                    account = account,
                    qrId = parameter,
                ).map { qr -> Result.success(qr) }
        }
}
