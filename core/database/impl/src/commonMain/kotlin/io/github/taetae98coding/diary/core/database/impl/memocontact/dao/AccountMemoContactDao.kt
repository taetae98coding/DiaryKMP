package io.github.taetae98coding.diary.core.database.impl.memocontact.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountMemoContactDao : RoomDao<AccountMemoContactLocalEntity> {
    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        INNER JOIN memo_contact
            ON memo_contact.contact_id = contact.id
                AND memo_contact.memo_id = :memoId
                AND memo_contact.is_deleted = 0
        INNER JOIN account_memo_contact
            ON account_memo_contact.memo_id = memo_contact.memo_id
                AND account_memo_contact.contact_id = memo_contact.contact_id
                AND account_memo_contact.account_id = :accountId
        INNER JOIN memo
            ON memo.id = memo_contact.memo_id
        WHERE contact.is_deleted = 0
        ORDER BY contact.name ASC
        """,
    )
    fun getContactList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<ContactLocalEntity>>

    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE contact.is_deleted = 0
            AND (
                :query = ''
                OR INSTR(LOWER(contact.name), LOWER(:query)) > 0
            )
        ORDER BY contact.name ASC
        """,
    )
    fun pageSelectableContact(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, ContactLocalEntity>

    @Query(
        """
        SELECT memo_contact.contact_id
        FROM memo_contact
        INNER JOIN account_memo_contact
            ON account_memo_contact.memo_id = memo_contact.memo_id
                AND account_memo_contact.contact_id = memo_contact.contact_id
                AND account_memo_contact.account_id = :accountId
        WHERE memo_contact.memo_id = :memoId AND memo_contact.is_deleted = 0
        """,
    )
    suspend fun findContactIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>

    @Query(
        """
        UPDATE account_memo_contact
        SET is_dirty = 1
        WHERE account_id = :accountId AND memo_id = :memoId AND contact_id = :contactId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
    ): Int
}
