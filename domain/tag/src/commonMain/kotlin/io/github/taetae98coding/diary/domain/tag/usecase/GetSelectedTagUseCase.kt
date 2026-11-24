package io.github.taetae98coding.diary.domain.tag.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
public class GetSelectedTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagRepository: AccountTagRepository,
) : FlowUseCase<Set<Uuid>, List<Tag>>() {
    override fun execute(parameter: Set<Uuid>): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagRepository
                .get(
                    account = account,
                    tagIdSet = parameter,
                ).map { tagList -> Result.success(tagList) }
        }
}
