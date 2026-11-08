package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountDDayDao : RoomDao<AccountDDayLocalEntity> {
    @Query(
        """
        WITH dday_filter_selected_tag AS (
            SELECT dday_filter_tag.tag_id
            FROM dday_filter_tag
            INNER JOIN tag
                ON tag.id = dday_filter_tag.tag_id
                    AND tag.is_finished = 0
                    AND tag.is_deleted = 0
            INNER JOIN account_tag
                ON account_tag.tag_id = dday_filter_tag.tag_id AND account_tag.account_id = :accountId
            WHERE dday_filter_tag.account_id = :accountId
        )
        SELECT dday.*
        FROM dday
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE dday.is_deleted = 0
            AND (
                NOT EXISTS(SELECT 1 FROM dday_filter_selected_tag)
                OR EXISTS(
                    SELECT 1
                    FROM dday_tag
                    INNER JOIN account_dday_tag
                        ON account_dday_tag.dday_id = dday_tag.dday_id
                            AND account_dday_tag.tag_id = dday_tag.tag_id
                            AND account_dday_tag.account_id = :accountId
                    WHERE dday_tag.dday_id = dday.id
                        AND dday_tag.is_deleted = 0
                        AND dday_tag.tag_id IN (SELECT tag_id FROM dday_filter_selected_tag)
                )
            )
        ORDER BY dday.title ASC
        """,
    )
    fun page(accountId: Uuid): PagingSource<Int, DDayLocalEntity>

    @Query(
        """
        SELECT dday.*
        FROM dday
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE dday.id = :ddayId
        """,
    )
    fun find(
        accountId: Uuid,
        ddayId: Uuid,
    ): Flow<DDayLocalEntity?>

    @Query(
        """
        UPDATE dday
        SET title = :title,
            description = :description,
            color = :color,
            base_date = :baseDate,
            counts_base_day = :countsBaseDay,
            repeats = :repeats,
            end_date = :endDate,
            overflow_policy = :overflowPolicy,
            updated_at = :updatedAt
        WHERE id = :ddayId
            AND EXISTS(
                SELECT 1
                FROM account_dday
                WHERE account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
            )
        """,
    )
    @Suppress("LongParameterList")
    suspend fun updateDetail(
        accountId: Uuid,
        ddayId: Uuid,
        title: String,
        description: String,
        color: Long,
        baseDate: LocalDate,
        countsBaseDay: Boolean,
        repeats: Set<DateTimeUnit.DateBased>,
        endDate: LocalDate?,
        overflowPolicy: DDayOverflowPolicyLocalEntity,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE dday
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :ddayId
            AND EXISTS(
                SELECT 1
                FROM account_dday
                WHERE account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        ddayId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_dday
        SET is_dirty = 1
        WHERE account_id = :accountId AND dday_id = :ddayId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        ddayId: Uuid,
    ): Int
}
