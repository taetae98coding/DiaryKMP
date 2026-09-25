package io.github.taetae98coding.diary.core.database.impl.sync.dao

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface SyncPendingDao {
    // 열세 종류를 한 번의 조회로 확인한다. 어느 종류가 남았는지는 쓰지 않으므로 첫 대기 항목을 찾으면 더 보지 않는다.
    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM account_tag WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_place WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_web WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_contact WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_music WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_memo WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_memo_tag WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_memo_place WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_memo_web WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_memo_contact WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_tag_link WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_web_tag WHERE account_id = :accountId AND is_dirty = 1)
            OR EXISTS(SELECT 1 FROM account_place_tag WHERE account_id = :accountId AND is_dirty = 1)
        """,
    )
    fun hasPending(accountId: Uuid): Flow<Boolean>
}
