package io.github.taetae98coding.diary.domain.place.usecase

import androidx.paging.PagingData
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
public class PagePlaceSelectableTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceTagRepository: AccountPlaceTagRepository,
) : FlowUseCase<PagePlaceSelectableTagUseCase.Parameter, PagingData<Tag>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountPlaceTagRepository
                .pageSelectableTag(
                    account = account,
                    placeId = parameter.placeId,
                    query = parameter.query.trim(),
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val placeId: Uuid,
        val query: String,
    )
}
