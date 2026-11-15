package io.github.taetae98coding.diary.domain.share

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

public sealed interface ShareTarget {
    public data class Web(
        val title: String,
        val url: String,
    ) : ShareTarget

    public data class Memo(
        val title: String,
        val description: String,
        val period: SharePeriod? = null,
    ) : ShareTarget

    public data class Contact(
        val name: String,
        val description: String,
        val phoneNumberList: List<String>,
        val birthday: LocalDate?,
    ) : ShareTarget
}

public sealed interface SharePeriod {
    public data class AllDay(
        val start: LocalDate,
        val endInclusive: LocalDate,
    ) : SharePeriod

    public data class DateTime(
        val start: LocalDateTime,
        val endInclusive: LocalDateTime,
    ) : SharePeriod
}
