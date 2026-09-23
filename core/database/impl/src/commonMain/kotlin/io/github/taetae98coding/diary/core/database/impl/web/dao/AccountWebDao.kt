package io.github.taetae98coding.diary.core.database.impl.web.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountWebDao : RoomDao<AccountWebLocalEntity> {
    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN web.updated_at END DESC,
            web.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, WebLocalEntity>

    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0 AND web.id IN (:webIdSet)
        ORDER BY web.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        webIdSet: Set<Uuid>,
    ): Flow<List<WebLocalEntity>>

    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.id = :webId
        """,
    )
    fun find(
        accountId: Uuid,
        webId: Uuid,
    ): Flow<WebLocalEntity?>

    @Query(
        """
        UPDATE web
        SET title = :title, description = :description, url = :url,
            header_list = :headerList, updated_at = :updatedAt
        WHERE id = :webId
            AND EXISTS(
                SELECT 1
                FROM account_web
                WHERE account_web.web_id = web.id AND account_web.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        webId: Uuid,
        title: String,
        description: String,
        url: String,
        headerList: List<WebHeaderLocalEntity>,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE web
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :webId
            AND EXISTS(
                SELECT 1
                FROM account_web
                WHERE account_web.web_id = web.id AND account_web.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_web
        SET is_dirty = 1
        WHERE account_id = :accountId AND web_id = :webId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        webId: Uuid,
    ): Int
}
