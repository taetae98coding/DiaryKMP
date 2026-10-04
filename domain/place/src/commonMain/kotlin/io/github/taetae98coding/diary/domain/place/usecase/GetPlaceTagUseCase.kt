package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetPlaceTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceTagRepository: AccountPlaceTagRepository,
) : FlowUseCase<Uuid, List<Tag>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountPlaceTagRepository
                .getTagList(
                    account = account,
                    placeId = parameter,
                ).map { tagList -> Result.success(tagList) }
        }
}
