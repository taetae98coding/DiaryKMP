package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.exception.TagTitleBlankException
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountTagRepository: AccountTagRepository,
    private val clock: Clock,
) : UseCase<AddTagUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        if (parameter.detail.title.isBlank()) throw TagTitleBlankException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val tag =
            Tag(
                id = Uuid.random(),
                detail = parameter.detail,
                isFinished = false,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountTagRepository.upsert(
            account = account,
            tag = tag,
            linkedTagIdSet = parameter.linkedTagIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return tag.id
    }

    public data class Parameter(
        val detail: TagDetail,
        val linkedTagIdSet: Set<Uuid> = emptySet(),
    )
}
