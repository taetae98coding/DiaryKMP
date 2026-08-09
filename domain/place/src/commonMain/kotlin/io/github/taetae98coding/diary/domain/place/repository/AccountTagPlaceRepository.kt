package io.github.taetae98coding.diary.domain.place.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagPlaceRepository {
    public fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<Place>>

    public fun get(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        bounds: CoordinateBounds,
        sort: ListSort,
    ): Flow<List<Place>>
}
