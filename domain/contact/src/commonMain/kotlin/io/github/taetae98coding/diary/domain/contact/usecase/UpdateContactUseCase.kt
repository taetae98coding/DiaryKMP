package io.github.taetae98coding.diary.domain.contact.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Inject
public class UpdateContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountContactRepository: AccountContactRepository,
    private val clock: Clock,
) : UseCase<UpdateContactUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.detail.phoneNumberList.any { phoneNumber -> phoneNumber.number.isBlank() }) {
            throw ContactPhoneNumberBlankException()
        }

        val account = getAccountUseCase.requireAccount()
        val detail =
            parameter.detail
                .withTrimmedName()
                .withStoredNameForBlank(account = account, id = parameter.id)
                .withBirthYearOfBirthday()

        val updatedCount =
            accountContactRepository.updateDetail(
                account = account,
                contactId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun ContactDetail.withStoredNameForBlank(
        account: Account,
        id: Uuid,
    ): ContactDetail {
        if (name.isNotBlank()) return this

        val stored =
            accountContactRepository
                .find(account = account, contactId = id)
                .first()
                ?.detail

        return copy(name = stored?.name.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val detail: ContactDetail,
    )
}
