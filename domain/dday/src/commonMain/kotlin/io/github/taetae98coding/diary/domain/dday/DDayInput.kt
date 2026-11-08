package io.github.taetae98coding.diary.domain.dday

import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import kotlinx.datetime.LocalDate

public data class DDayInput(
    val title: String,
    val description: String,
    val color: Long,
    val baseDate: LocalDate,
    val countsBaseDay: Boolean,
    val repeatList: List<DDayRepeatInput>,
    val endDate: LocalDate?,
    val overflowPolicy: DDayOverflowPolicy,
)

internal fun DDayInput.toDetail(): DDayDetail =
    DDayDetail(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeatList.toRepeatSetOrNull() ?: throw DDayRepeatBlankException(),
        endDate = endDate,
        overflowPolicy = overflowPolicy,
    )
