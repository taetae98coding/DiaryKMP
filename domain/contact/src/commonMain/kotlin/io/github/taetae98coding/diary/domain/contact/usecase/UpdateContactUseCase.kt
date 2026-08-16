package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
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
public class UpdateContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findContactUseCase: FindContactUseCase,
    private val accountContactRepository: AccountContactRepository,
    private val clock: Clock,
) : UseCase<UpdateContactUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.detail.phoneNumberList.any { phoneNumber -> phoneNumber.number.isBlank() }) {
            throw ContactPhoneNumberBlankException()
        }

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val detail = parameter.detail.withStoredNameForBlank(id = parameter.id)

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

    // 이름은 연락처가 반드시 가져야 하는 정보이므로 비우는 수정을 저장된 값으로 되돌린다.
    private suspend fun ContactDetail.withStoredNameForBlank(id: Uuid): ContactDetail {
        if (name.isNotBlank()) return this

        val stored =
            findContactUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(name = stored?.name.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val detail: ContactDetail,
    )
}
