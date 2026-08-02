package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountTagRepository: AccountTagRepository,
    private val clock: Clock,
) : UseCase<UpdateTagUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val detail =
            parameter.detail.copy(
                title =
                    parameter.detail.title.ifBlank {
                        findTagUseCase(parameter.id)
                            .first()
                            .getOrThrow()
                            ?.detail
                            ?.title
                            .orEmpty()
                    },
            )

        val count =
            accountTagRepository.updateDetail(
                account = account,
                tagId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return count
    }

    public data class Parameter(
        val id: Uuid,
        val detail: TagDetail,
    )
}
