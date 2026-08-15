package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebTitleBlankException
import io.github.taetae98coding.diary.domain.web.exception.WebUrlBlankException
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountWebRepository: AccountWebRepository,
    private val clock: Clock,
) : UseCase<AddWebUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        parameter.detail.blankException()?.let { exception -> throw exception }

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val web =
            Web(
                id = Uuid.random(),
                detail = parameter.detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountWebRepository.upsert(
            account = account,
            web = web,
            tagIdSet = parameter.tagIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return web.id
    }

    private fun WebDetail.blankException(): Exception? =
        when {
            title.isBlank() -> WebTitleBlankException()
            url.isBlank() -> WebUrlBlankException()
            headerList.any { header -> header.name.isBlank() } -> WebHeaderNameBlankException()
            else -> null
        }

    public data class Parameter(
        val detail: WebDetail,
        val tagIdSet: Set<Uuid>,
    )
}
