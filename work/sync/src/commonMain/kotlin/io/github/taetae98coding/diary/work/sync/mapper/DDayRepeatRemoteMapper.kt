package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity
import kotlinx.datetime.DateTimeUnit

internal fun DateTimeUnit.DateBased.toRemote(): DDayRepeatRemoteEntity =
    when (this) {
        is DateTimeUnit.DayBased -> DDayRepeatRemoteEntity.Days(days = days)
        is DateTimeUnit.MonthBased -> DDayRepeatRemoteEntity.Months(months = months)
    }

internal fun DDayRepeatRemoteEntity.toLocal(): DateTimeUnit.DateBased =
    when (this) {
        is DDayRepeatRemoteEntity.Days -> DateTimeUnit.DayBased(days = days)
        is DDayRepeatRemoteEntity.Months -> DateTimeUnit.MonthBased(months = months)
    }
