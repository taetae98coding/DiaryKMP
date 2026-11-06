package io.github.taetae98coding.diary.domain.playlist.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountTagMusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagMusicRepository: AccountTagMusicRepository,
) : FlowUseCase<PageTagMusicUseCase.Parameter, PagingData<Music>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Music>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagMusicRepository
                .page(
                    account = account,
                    tagId = parameter.tagId,
                    scope = parameter.scope,
                    sort = parameter.sort,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
        val sort: ListSort,
    )
}
