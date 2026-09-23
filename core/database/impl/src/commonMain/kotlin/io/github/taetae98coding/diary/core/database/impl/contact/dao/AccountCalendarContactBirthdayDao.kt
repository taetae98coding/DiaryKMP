package io.github.taetae98coding.diary.core.database.impl.contact.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
internal interface AccountCalendarContactBirthdayDao {
    // SQLite의 date()는 2월 29일처럼 그 해에 없는 날짜를 3월 1일로 넘겨 계산하고 NULL을 주지 않으므로,
    // date(birthday_date) = birthday_date로 넘어간 날짜를 걸러 그 해에 실제로 있는 월·일만 남긴다.
    @Query(
        """
        WITH target_year(year) AS (
            SELECT CAST(strftime('%Y', date(:start)) AS INTEGER)
            UNION
            SELECT CAST(strftime('%Y', date(:endInclusive)) AS INTEGER)
        ),
        birthday_occurrence AS (
            SELECT contact.id AS contact_id, contact.name AS name,
                printf('%04d', target_year.year) || substr(contact.birthday, 5) AS birthday_date
            FROM contact
            INNER JOIN account_contact
                ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
            CROSS JOIN target_year
            WHERE contact.is_deleted = 0
                AND contact.birthday IS NOT NULL
                AND target_year.year >= CAST(strftime('%Y', contact.birthday) AS INTEGER)
        )
        SELECT contact_id, name, birthday_date
        FROM birthday_occurrence
        WHERE date(birthday_date) = birthday_date
            AND birthday_date >= date(:start)
            AND birthday_date <= date(:endInclusive)
        ORDER BY birthday_date ASC, name ASC, contact_id ASC
        """,
    )
    fun get(
        accountId: Uuid,
        start: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<CalendarContactBirthdayLocalEntity>>
}
