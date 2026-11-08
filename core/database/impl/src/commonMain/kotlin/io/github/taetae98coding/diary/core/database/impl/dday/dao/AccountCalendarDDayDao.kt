package io.github.taetae98coding.diary.core.database.impl.dday.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Dao
internal interface AccountCalendarDDayDao {
    // 기념일의 번째(k)는 표시 대상 기간에 걸릴 수 있는 범위만 계산해 펼친다. 기준일에서 1부터 펼치면 먼 미래의 기간일수록 행이 늘어난다.
    // SQLite의 date(x, '+N months')는 1월 31일 + 1개월을 3월 3일로 넘기므로 쓰지 않고, 목표 연·월을 구해 그 달의 마지막 날과 비교한다.
    // 한 디데이의 같은 날짜는 GROUP BY와 MIN(priority)로 합친다. SQLite는 MIN·MAX 집계 하나만 있으면 나머지 열을 그 최솟값의 행에서 가져온다.
    @Query(
        """
        WITH RECURSIVE calendar_filter_selected_tag AS (
            SELECT calendar_filter_tag.tag_id
            FROM calendar_filter_tag
            INNER JOIN tag
                ON tag.id = calendar_filter_tag.tag_id
                    AND tag.is_finished = 0
                    AND tag.is_deleted = 0
            INNER JOIN account_tag
                ON account_tag.tag_id = calendar_filter_tag.tag_id AND account_tag.account_id = :accountId
            WHERE calendar_filter_tag.account_id = :accountId
        ),
        target_dday AS (
            SELECT dday.id AS dday_id, dday.base_date AS base_date, dday.counts_base_day AS counts_base_day,
                dday.repeats AS repeats, dday.overflow_policy AS overflow_policy,
                CASE
                    WHEN dday.end_date IS NOT NULL AND dday.end_date < date(:endInclusive) THEN dday.end_date
                    ELSE date(:endInclusive)
                END AS last_date
            FROM dday
            INNER JOIN account_dday
                ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
            WHERE dday.is_deleted = 0
                AND dday.base_date <= date(:endInclusive)
                AND (
                    NOT EXISTS(SELECT 1 FROM calendar_filter_selected_tag)
                    OR EXISTS(
                        SELECT 1
                        FROM dday_tag
                        INNER JOIN account_dday_tag
                            ON account_dday_tag.dday_id = dday_tag.dday_id
                                AND account_dday_tag.tag_id = dday_tag.tag_id
                                AND account_dday_tag.account_id = :accountId
                        WHERE dday_tag.dday_id = dday.id
                            AND dday_tag.is_deleted = 0
                            AND dday_tag.tag_id IN (SELECT tag_id FROM calendar_filter_selected_tag)
                    )
                )
        ),
        day_bound AS (
            SELECT target_dday.dday_id AS dday_id, target_dday.base_date AS base_date,
                CAST(json_extract(repeat.value, '$.days') AS INTEGER) AS days,
                target_dday.counts_base_day AS base_day_offset,
                MAX(
                    1,
                    (
                        CAST(julianday(date(:start)) - julianday(target_dday.base_date) AS INTEGER)
                            + target_dday.counts_base_day
                            + CAST(json_extract(repeat.value, '$.days') AS INTEGER) - 1
                    ) / CAST(json_extract(repeat.value, '$.days') AS INTEGER)
                ) AS first_k,
                (
                    CAST(julianday(target_dday.last_date) - julianday(target_dday.base_date) AS INTEGER)
                        + target_dday.counts_base_day
                ) / CAST(json_extract(repeat.value, '$.days') AS INTEGER) AS last_k
            FROM target_dday, json_each(target_dday.repeats) AS repeat
            WHERE json_extract(repeat.value, '$.days') IS NOT NULL
        ),
        day_occurrence(dday_id, base_date, days, base_day_offset, k, last_k) AS (
            SELECT dday_id, base_date, days, base_day_offset, first_k, last_k
            FROM day_bound
            WHERE first_k <= last_k
            UNION ALL
            SELECT dday_id, base_date, days, base_day_offset, k + 1, last_k
            FROM day_occurrence
            WHERE k < last_k
        ),
        month_repeat AS (
            SELECT target_dday.dday_id AS dday_id, target_dday.overflow_policy AS overflow_policy,
                target_dday.last_date AS last_date,
                CAST(json_extract(repeat.value, '$.months') AS INTEGER) AS months,
                CAST(strftime('%Y', target_dday.base_date) AS INTEGER) * 12
                    + CAST(strftime('%m', target_dday.base_date) AS INTEGER) - 1 AS base_month_index,
                CAST(strftime('%d', target_dday.base_date) AS INTEGER) AS base_day,
                CAST(strftime('%Y', date(:start)) AS INTEGER) * 12
                    + CAST(strftime('%m', date(:start)) AS INTEGER) - 1 AS start_month_index,
                CAST(strftime('%Y', target_dday.last_date) AS INTEGER) * 12
                    + CAST(strftime('%m', target_dday.last_date) AS INTEGER) - 1 AS last_month_index
            FROM target_dday, json_each(target_dday.repeats) AS repeat
            WHERE json_extract(repeat.value, '$.months') IS NOT NULL
        ),
        month_bound AS (
            SELECT dday_id, overflow_policy, last_date, months, base_month_index, base_day,
                MAX(1, (start_month_index - 1 - base_month_index + months - 1) / months) AS first_k,
                (last_month_index - base_month_index) / months AS last_k
            FROM month_repeat
        ),
        month_occurrence(dday_id, overflow_policy, last_date, months, base_month_index, base_day, k, last_k) AS (
            SELECT dday_id, overflow_policy, last_date, months, base_month_index, base_day, first_k, last_k
            FROM month_bound
            WHERE first_k <= last_k
            UNION ALL
            SELECT dday_id, overflow_policy, last_date, months, base_month_index, base_day, k + 1, last_k
            FROM month_occurrence
            WHERE k < last_k
        ),
        month_target AS (
            SELECT dday_id, overflow_policy, last_date, months, k, base_day,
                (base_month_index + k * months) / 12 AS target_year,
                (base_month_index + k * months) % 12 + 1 AS target_month
            FROM month_occurrence
        ),
        month_target_last_day AS (
            SELECT dday_id, overflow_policy, last_date, months, k, base_day, target_year, target_month,
                printf('%04d-%02d-01', target_year, target_month) AS target_month_first_date,
                CAST(strftime('%d', printf('%04d-%02d-01', target_year, target_month), '+1 month', '-1 day') AS INTEGER) AS target_month_last_day
            FROM month_target
        ),
        month_occurrence_date AS (
            SELECT dday_id, last_date, months, k,
                CASE
                    WHEN base_day <= target_month_last_day THEN printf('%04d-%02d-%02d', target_year, target_month, base_day)
                    WHEN overflow_policy = :clampToMonthEnd THEN printf('%04d-%02d-%02d', target_year, target_month, target_month_last_day)
                    WHEN overflow_policy = :nextMonthFirstDay THEN date(target_month_first_date, '+1 month')
                    ELSE NULL
                END AS occurrence_date
            FROM month_target_last_day
        ),
        candidate(dday_id, occurrence_date, priority, kind, amount) AS (
            SELECT dday_id, base_date, 0, :baseDayKind, 0
            FROM target_dday
            UNION ALL
            SELECT dday_id, date(base_date, '+' || (k * days - base_day_offset) || ' days'), 3, :daysKind, k * days
            FROM day_occurrence
            UNION ALL
            SELECT dday_id, occurrence_date,
                CASE WHEN months % 12 = 0 THEN 1 ELSE 2 END,
                CASE WHEN months % 12 = 0 THEN :yearsKind ELSE :monthsKind END,
                CASE WHEN months % 12 = 0 THEN k * months / 12 ELSE k * months END
            FROM month_occurrence_date
            WHERE occurrence_date IS NOT NULL AND occurrence_date <= last_date
        ),
        merged AS (
            SELECT dday_id, occurrence_date, MIN(priority) AS priority, kind, amount
            FROM candidate
            WHERE occurrence_date >= date(:start) AND occurrence_date <= date(:endInclusive)
            GROUP BY dday_id, occurrence_date
        )
        SELECT dday.id AS dday_id, dday.title AS title, dday.color AS color,
            merged.occurrence_date AS date, merged.kind AS kind, merged.amount AS amount
        FROM merged
        INNER JOIN dday
            ON dday.id = merged.dday_id
        ORDER BY merged.occurrence_date ASC, dday.title ASC, dday.id ASC
        """,
    )
    @Suppress("LongParameterList")
    fun get(
        accountId: Uuid,
        start: LocalDate,
        endInclusive: LocalDate,
        clampToMonthEnd: DDayOverflowPolicyLocalEntity,
        nextMonthFirstDay: DDayOverflowPolicyLocalEntity,
        baseDayKind: CalendarDDayKindLocalEntity,
        yearsKind: CalendarDDayKindLocalEntity,
        monthsKind: CalendarDDayKindLocalEntity,
        daysKind: CalendarDDayKindLocalEntity,
    ): Flow<List<CalendarDDayLocalEntity>>
}
