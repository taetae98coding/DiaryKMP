package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

public interface AccountDailyMemoRepository {
    public fun get(
        account: Account,
        date: LocalDate,
    ): Flow<List<DailyMemo>>
}
