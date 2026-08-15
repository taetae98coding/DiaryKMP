package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.exception.WebHeaderNameBlankException
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val accountWebRepository: AccountWebRepository,
    private val clock: Clock,
) : UseCase<UpdateWebUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.detail.headerList.any { header -> header.name.isBlank() }) throw WebHeaderNameBlankException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val detail = parameter.detail.withStoredValueForBlank(id = parameter.id)

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

    // 제목과 URL은 웹 항목이 반드시 가져야 하는 정보이므로 비우는 수정을 저장된 값으로 되돌린다.
    private suspend fun WebDetail.withStoredValueForBlank(id: Uuid): WebDetail {
        if (title.isNotBlank() && url.isNotBlank()) return this

        val stored =
            findWebUseCase(parameter = id)
                .first()
                .getOrThrow()
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
