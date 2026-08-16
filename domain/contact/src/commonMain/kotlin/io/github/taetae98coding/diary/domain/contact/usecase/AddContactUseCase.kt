package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.exception.ContactNameBlankException
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountContactRepository: AccountContactRepository,
    private val clock: Clock,
) : UseCase<AddContactUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        parameter.detail.blankException()?.let { exception -> throw exception }

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val contact =
            Contact(
                id = Uuid.random(),
                detail = parameter.detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountContactRepository.upsert(
            account = account,
            contact = contact,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return contact.id
    }

    private fun ContactDetail.blankException(): Exception? =
        when {
            name.isBlank() -> ContactNameBlankException()
            phoneNumberList.any { phoneNumber -> phoneNumber.number.isBlank() } -> ContactPhoneNumberBlankException()
            else -> null
        }

    public data class Parameter(
        val detail: ContactDetail,
    )
}
