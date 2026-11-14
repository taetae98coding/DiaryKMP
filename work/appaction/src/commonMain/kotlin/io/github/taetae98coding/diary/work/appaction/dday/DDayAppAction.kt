package io.github.taetae98coding.diary.work.appaction.dday

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatInputList
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrRandom
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

private const val BASE_DATE = "baseDate"
private const val DDAY_ID = "ddayId"
private const val END_DATE = "endDate"

@Factory
public class DDayAppAction internal constructor(
    private val pageDDayUseCase: PageDDayUseCase,
    private val findDDayUseCase: FindDDayUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addDDayUseCase: AddDDayUseCase,
    private val updateDDayUseCase: UpdateDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    private val restoreDDayUseCase: RestoreDDayUseCase,
    private val clock: Clock,
) {
    public suspend fun list(cursor: String?): AppActionPage<AppActionDDay> =
        runAppAction {
            val today = today()

            pageDDayUseCase
                .readPage(parameter = Unit, offset = cursor.toPageOffset())
                .map { dday -> dday.toAppActionDDay(today = today) }
        }

    public suspend fun get(ddayId: String): AppActionDDayDetail? = runAppAction { detail(ddayId = ddayId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(ddayId: String): AppActionDDayDetail? = runAppAction { detail(ddayId = ddayId, includesDeleted = true) }

    private suspend fun detail(
        ddayId: String,
        includesDeleted: Boolean,
    ): AppActionDDayDetail? {
        val id = ddayId.toUuidArgument(name = DDAY_ID)
        val dday = findDDayUseCase.readFirst(parameter = id)?.takeIf { dday -> includesDeleted || !dday.isDeleted } ?: return null

        return AppActionDDayDetail(
            dday = dday.toAppActionDDay(today = today()),
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.DDAY, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
        title: String,
        description: String?,
        color: String?,
        baseDate: String?,
        countsBaseDay: Boolean?,
        repeats: List<String>,
        endDate: String?,
        overflowPolicy: String?,
        tagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val input =
                DDayInput(
                    title = title,
                    description = description.orEmpty(),
                    color = color.toColorArgumentOrRandom(),
                    baseDate = baseDate.toLocalDateArgumentOrNull(name = BASE_DATE) ?: today(),
                    countsBaseDay = countsBaseDay ?: true,
                    repeatList = repeats.toDDayRepeatInputListArgument(),
                    endDate = endDate.toLocalDateArgumentOrNull(name = END_DATE),
                    overflowPolicy = overflowPolicy.toDDayOverflowPolicyArgumentOrNull() ?: DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                ).requireEndDateNotBeforeBaseDate()
            val parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"))

            addDDayUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    // 반복 간격과 종료일은 담지 않음과 지움을 구분해야 하므로, 반복 간격은 null로 담지 않음을, 종료일은 clearEndDate로 지움을 받는다.
    public suspend fun update(
        ddayId: String,
        title: String?,
        description: String?,
        color: String?,
        baseDate: String?,
        countsBaseDay: Boolean?,
        repeats: List<String>?,
        endDate: String?,
        clearEndDate: Boolean?,
        overflowPolicy: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = ddayId.toUuidArgument(name = DDAY_ID)
            val newBaseDate = baseDate.toLocalDateArgumentOrNull(name = BASE_DATE)
            val newEndDate = endDate.toLocalDateArgumentOrNull(name = END_DATE)
            val newColor = color.toColorArgumentOrNull()
            val newRepeatList = repeats?.toDDayRepeatInputListArgument()
            val newOverflowPolicy = overflowPolicy.toDDayOverflowPolicyArgumentOrNull()
            val current = findDDayUseCase.readActive(id = id, isDeleted = DDay::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val input =
                DDayInput(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    color = newColor ?: current.color,
                    baseDate = newBaseDate ?: current.baseDate,
                    countsBaseDay = countsBaseDay ?: current.countsBaseDay,
                    repeatList = newRepeatList ?: current.repeats.toRepeatInputList(),
                    endDate = if (clearEndDate == true) null else newEndDate ?: current.endDate,
                    overflowPolicy = newOverflowPolicy ?: current.overflowPolicy,
                ).requireEndDateNotBeforeBaseDate()

            updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun delete(ddayId: String): AppActionChangedCount =
        runAppAction {
            val id = ddayId.toUuidArgument(name = DDAY_ID)

            findDDayUseCase.changeActive(id = id, isDeleted = DDay::isDeleted) { deleteDDayUseCase(parameter = id) }
        }

    public suspend fun restore(ddayId: String): AppActionChangedCount = runAppAction { restoreDDayUseCase(parameter = ddayId.toUuidArgument(name = DDAY_ID)).getOrThrow().toAppActionChangedCount() }

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
}

private fun DDayInput.requireEndDateNotBeforeBaseDate(): DDayInput {
    val endDate = endDate ?: return this

    if (endDate < baseDate) invalidArgument(message = "'$END_DATE' must not be before '$BASE_DATE'.")

    return this
}
