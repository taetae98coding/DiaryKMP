package io.github.taetae98coding.diary.domain.web.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Inject
public class UpdateWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountWebRepository: AccountWebRepository,
    private val clock: Clock,
) : UseCase<UpdateWebUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.detail.headerList.any { header -> header.name.isBlank() }) throw WebHeaderNameBlankException()

        val account = getAccountUseCase.requireAccount()
        val detail = parameter.detail.withStoredValueForBlank(account = account, id = parameter.id)

        val updatedCount =
            accountWebRepository.updateDetail(
                account = account,
                webId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun WebDetail.withStoredValueForBlank(
        account: Account,
        id: Uuid,
    ): WebDetail {
        if (title.isNotBlank() && url.isNotBlank()) return this

        val stored =
            accountWebRepository
                .find(account = account, webId = id)
                .first()
                ?.detail

        return copy(
            title = title.ifBlank { stored?.title.orEmpty() },
            url = url.ifBlank { stored?.url.orEmpty() },
        )
    }

    public data class Parameter(
        val id: Uuid,
        val detail: WebDetail,
    )
}
