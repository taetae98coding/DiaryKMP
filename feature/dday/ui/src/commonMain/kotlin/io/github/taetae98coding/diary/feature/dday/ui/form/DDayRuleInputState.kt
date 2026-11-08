package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.github.taetae98coding.diary.domain.dday.preset.DDayPresetValue
import io.github.taetae98coding.diary.domain.dday.preset.value
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Stable
internal class DDayRuleInputState(
    baseDate: LocalDate,
    countsBaseDay: Boolean,
    endDate: LocalDate?,
    overflowPolicy: DDayOverflowPolicy,
    repeatRowList: List<DDayRepeatRowState>,
) {
    var baseDate: LocalDate by mutableStateOf(baseDate)
        private set

    var countsBaseDay: Boolean by mutableStateOf(countsBaseDay)
        private set

    var endDate: LocalDate? by mutableStateOf(endDate)
        private set

    var overflowPolicy: DDayOverflowPolicy by mutableStateOf(overflowPolicy)
        private set

    private val repeatRowStateList: SnapshotStateList<DDayRepeatRowState> = repeatRowList.toMutableStateList()

    val repeatRowList: List<DDayRepeatRowState>
        get() = repeatRowStateList

    val repeatInputList: List<DDayRepeatInput>
        get() = repeatRowStateList.map { row -> row.input }

    fun selectBaseDate(date: LocalDate) {
        baseDate = date
        endDate?.let { value -> if (value < date) endDate = date }
    }

    fun updateCountsBaseDay(countsBaseDay: Boolean) {
        this.countsBaseDay = countsBaseDay
    }

    fun selectEndDate(date: LocalDate) {
        endDate = date
    }

    fun clearEndDate() {
        endDate = null
    }

    fun selectOverflowPolicy(overflowPolicy: DDayOverflowPolicy) {
        this.overflowPolicy = overflowPolicy
    }

    fun addRepeat(): DDayRepeatRowState {
        val row = DDayRepeatRowState(key = Uuid.random().toString(), initialNumber = "", initialUnit = DDayRepeatUnit.DAY)
        repeatRowStateList.add(row)
        return row
    }

    fun removeRepeat(key: String) {
        repeatRowStateList.removeAll { row -> row.key == key }
    }

    fun applyPreset(value: DDayPresetValue) {
        countsBaseDay = value.countsBaseDay
        endDate = value.endDate
        overflowPolicy = value.overflowPolicy
        repeatRowStateList.clear()
        repeatRowStateList.addAll(value.repeatList.toRepeatRowList())
    }

    fun reset(baseDate: LocalDate) {
        this.baseDate = baseDate
        applyPreset(value = DDayPreset.CUSTOM.value())
    }

    companion object {
        private const val BASE_DATE_KEY = "baseDate"
        private const val COUNTS_BASE_DAY_KEY = "countsBaseDay"
        private const val END_DATE_KEY = "endDate"
        private const val OVERFLOW_POLICY_KEY = "overflowPolicy"
        private const val REPEAT_KEY = "repeat"
        private const val REPEAT_FIELD_COUNT = 3

        val Saver: Saver<DDayRuleInputState, Any> =
            mapSaver(
                save = { state ->
                    mapOf(
                        BASE_DATE_KEY to state.baseDate.toEpochDays(),
                        COUNTS_BASE_DAY_KEY to state.countsBaseDay,
                        END_DATE_KEY to state.endDate?.toEpochDays(),
                        OVERFLOW_POLICY_KEY to state.overflowPolicy.name,
                        REPEAT_KEY to state.repeatRowStateList.flatMap { row -> listOf(row.key, row.number, row.unit.name) },
                    )
                },
                restore = { saved ->
                    @Suppress("UNCHECKED_CAST")
                    val repeat = saved[REPEAT_KEY] as List<String>

                    DDayRuleInputState(
                        baseDate = LocalDate.fromEpochDays(saved[BASE_DATE_KEY] as Long),
                        countsBaseDay = saved[COUNTS_BASE_DAY_KEY] as Boolean,
                        endDate = (saved[END_DATE_KEY] as Long?)?.let(LocalDate::fromEpochDays),
                        overflowPolicy = DDayOverflowPolicy.valueOf(saved[OVERFLOW_POLICY_KEY] as String),
                        repeatRowList =
                            repeat.chunked(REPEAT_FIELD_COUNT) { (key, number, unit) ->
                                DDayRepeatRowState(key = key, initialNumber = number, initialUnit = DDayRepeatUnit.valueOf(unit))
                            },
                    )
                },
            )
    }
}

@Composable
internal fun rememberDDayRuleInputState(
    initialBaseDate: LocalDate,
    initialCountsBaseDay: Boolean,
    initialEndDate: LocalDate?,
    initialOverflowPolicy: DDayOverflowPolicy,
    initialRepeatList: List<DDayRepeatInput>,
): DDayRuleInputState =
    rememberSaveable(saver = DDayRuleInputState.Saver) {
        DDayRuleInputState(
            baseDate = initialBaseDate,
            countsBaseDay = initialCountsBaseDay,
            endDate = initialEndDate,
            overflowPolicy = initialOverflowPolicy,
            repeatRowList = initialRepeatList.toRepeatRowList(),
        )
    }

private fun List<DDayRepeatInput>.toRepeatRowList(): List<DDayRepeatRowState> =
    map { input ->
        DDayRepeatRowState(
            key = Uuid.random().toString(),
            initialNumber = input.number?.toString().orEmpty(),
            initialUnit = input.unit,
        )
    }
