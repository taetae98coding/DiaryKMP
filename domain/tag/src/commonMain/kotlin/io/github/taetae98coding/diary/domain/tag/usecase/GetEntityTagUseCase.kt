package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.repository.AccountEntityTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetEntityTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountEntityTagRepository: AccountEntityTagRepository,
) : FlowUseCase<GetEntityTagUseCase.Parameter, List<Tag>>() {
    override fun execute(parameter: Parameter): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountEntityTagRepository
                .getTagList(
                    account = account,
                    kind = parameter.kind,
                    entityId = parameter.entityId,
                ).map { tagList -> Result.success(tagList) }
        }

    public data class Parameter(
        val kind: EntityTagKind,
        val entityId: Uuid,
    )
}
