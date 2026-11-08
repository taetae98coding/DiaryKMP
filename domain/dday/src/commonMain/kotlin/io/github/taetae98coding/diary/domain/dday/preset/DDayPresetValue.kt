package io.github.taetae98coding.diary.domain.dday.preset

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import kotlinx.datetime.LocalDate

public data class DDayPresetValue(
    val countsBaseDay: Boolean,
    val repeatList: List<DDayRepeatInput>,
    val endDate: LocalDate?,
    val overflowPolicy: DDayOverflowPolicy,
)

private const val HUNDRED: Int = 100

private val defaultValue: DDayPresetValue =
    DDayPresetValue(
        countsBaseDay = true,
        repeatList = emptyList(),
        endDate = null,
        overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
    )

public fun DDayPreset.value(): DDayPresetValue =
    when (this) {
        DDayPreset.COUPLE ->
            defaultValue.copy(
                repeatList =
                    listOf(
                        DDayRepeatInput(number = HUNDRED, unit = DDayRepeatUnit.DAY),
                        DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                    ),
            )

        DDayPreset.EXAM -> defaultValue

        DDayPreset.BABY ->
            defaultValue.copy(
                repeatList =
                    listOf(
                        DDayRepeatInput(number = HUNDRED, unit = DDayRepeatUnit.DAY),
                        DDayRepeatInput(number = 1, unit = DDayRepeatUnit.MONTH),
                        DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                    ),
            )

        DDayPreset.CUSTOM -> defaultValue
    }
