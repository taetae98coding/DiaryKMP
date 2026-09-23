package io.github.taetae98coding.diary.core.database.impl.contact.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountContactDao : RoomDao<AccountContactLocalEntity> {
    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE contact.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN contact.updated_at END DESC,
            contact.name ASC
        """,
    )
    fun page(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, ContactLocalEntity>

    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE contact.id = :contactId
        """,
    )
    fun find(
        accountId: Uuid,
        contactId: Uuid,
    ): Flow<ContactLocalEntity?>

    @Suppress("LongParameterList")
    @Query(
        """
        UPDATE contact
        SET name = :name, description = :description, height_centimeter = :heightCentimeter,
            foot_size_millimeter = :footSizeMillimeter, birthday = :birthday,
            birthday_calendar = :birthdayCalendar, phone_number_list = :phoneNumberList,
            updated_at = :updatedAt
        WHERE id = :contactId
            AND EXISTS(
                SELECT 1
                FROM account_contact
                WHERE account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        contactId: Uuid,
        name: String,
        description: String,
        heightCentimeter: Double?,
        footSizeMillimeter: Int?,
        birthday: LocalDate?,
        birthdayCalendar: ContactBirthdayCalendarLocalEntity?,
        phoneNumberList: List<ContactPhoneNumberLocalEntity>,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE contact
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :contactId
            AND EXISTS(
                SELECT 1
                FROM account_contact
                WHERE account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_contact
        SET is_dirty = 1
        WHERE account_id = :accountId AND contact_id = :contactId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        contactId: Uuid,
    ): Int
}
