package io.github.taetae98coding.diary.domain.contact.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
public class FindContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountContactRepository: AccountContactRepository,
) : FlowUseCase<Uuid, Contact?>() {
    override fun execute(parameter: Uuid): Flow<Result<Contact?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountContactRepository
                .find(
                    account = account,
                    contactId = parameter,
                ).map { contact -> Result.success(contact) }
        }
}
