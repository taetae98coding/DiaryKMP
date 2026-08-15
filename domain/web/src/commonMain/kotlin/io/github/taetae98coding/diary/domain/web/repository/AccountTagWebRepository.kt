package io.github.taetae98coding.diary.domain.web.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagWebRepository {
    public fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<Web>>
}
