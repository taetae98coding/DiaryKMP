package io.github.taetae98coding.diary.domain.tag.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
public class GetLinkedTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagLinkRepository: AccountTagLinkRepository,
) : FlowUseCase<Uuid, List<Tag>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagLinkRepository
                .getTagList(
                    account = account,
                    fromTagId = parameter,
                ).map { tagList -> Result.success(tagList) }
        }
}
