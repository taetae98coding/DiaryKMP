package io.github.taetae98coding.diary.domain.qr.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountTagQrRepository {
    public fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
    ): Flow<PagingData<Qr>>
}
