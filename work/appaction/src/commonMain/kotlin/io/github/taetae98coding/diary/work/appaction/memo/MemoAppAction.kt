package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.CopyMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.work.appaction.contact.toAppActionContact
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrRandom
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.place.toAppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import io.github.taetae98coding.diary.work.appaction.web.toAppActionWeb
import org.koin.core.annotation.Factory

private const val MEMO_ID = "memoId"

internal val memoSortList: List<ListSort> = listOf(ListSort.DEFAULT, ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class MemoAppAction internal constructor(
    private val pageMemoHomeUseCase: PageMemoHomeUseCase,
    private val pageFinishedMemoUseCase: PageFinishedMemoUseCase,
    private val findMemoUseCase: FindMemoUseCase,
    private val getMemoTagUseCase: GetMemoTagUseCase,
    private val getMemoPlaceUseCase: GetMemoPlaceUseCase,
    private val getMemoWebUseCase: GetMemoWebUseCase,
    private val getMemoContactUseCase: GetMemoContactUseCase,
    private val addMemoUseCase: AddMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val copyMemoUseCase: CopyMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val restoreMemoUseCase: RestoreMemoUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            pageMemoHomeUseCase
                .readPage(parameter = sort.toSortArgument(memoSortList), offset = cursor.toPageOffset())
                .map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun finishedList(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            pageFinishedMemoUseCase
                .readPage(parameter = sort.toSortArgument(memoSortList), offset = cursor.toPageOffset())
                .map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun get(memoId: String): AppActionMemoDetail? =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)
            val memo = findMemoUseCase.readFirst(parameter = id) ?: return@runAppAction null

            AppActionMemoDetail(
                memo = memo.toAppActionMemo(),
                tags = getMemoTagUseCase.readFirst(parameter = id).map { tag -> tag.toAppActionTag() },
                places = getMemoPlaceUseCase.readFirst(parameter = id).map { place -> place.toAppActionPlace() },
                webs = getMemoWebUseCase.readFirst(parameter = id).map { web -> web.toAppActionWeb() },
                contacts = getMemoContactUseCase.readFirst(parameter = id).map { contact -> contact.toAppActionContact() },
            )
        }

    public suspend fun add(
        title: String,
        description: String?,
        color: String?,
        dateTime: AppActionMemoDateTimeInput?,
        primaryTagId: String?,
        tagIds: List<String>,
        placeIds: List<String>,
        webIds: List<String>,
        contactIds: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddMemoUseCase.Parameter(
                    detail =
                        MemoDetail(
                            title = title,
                            description = description.orEmpty(),
                            color = color.toColorArgumentOrRandom(),
                            dateTime = dateTime?.toMemoDateTime(),
                        ),
                    primaryTagId = primaryTagId.toUuidArgumentOrNull(name = "primaryTagId"),
                    tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"),
                    placeIdSet = placeIds.toUuidSetArgument(name = "placeIds"),
                    webIdSet = webIds.toUuidSetArgument(name = "webIds"),
                    contactIdSet = contactIds.toUuidSetArgument(name = "contactIds"),
                )

            addMemoUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        memoId: String,
        title: String?,
        description: String?,
        color: String?,
        dateTime: AppActionMemoDateTimeInput?,
        clearDateTime: Boolean?,
    ): AppActionChangedCount =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)
            val current = findMemoUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                MemoDetail(
                    title = title ?: current.title,
                    description = description ?: current.description,
                    color = color.toColorArgumentOrNull() ?: current.color,
                    dateTime =
                        when {
                            clearDateTime == true -> null
                            dateTime != null -> dateTime.toMemoDateTime()
                            else -> current.dateTime
                        },
                )

            updateMemoUseCase(parameter = UpdateMemoUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun copy(memoId: String): AppActionId = runAppAction { copyMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionId() }

    public suspend fun finish(memoId: String): AppActionChangedCount = runAppAction { finishMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restart(memoId: String): AppActionChangedCount = runAppAction { restartMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun delete(memoId: String): AppActionChangedCount = runAppAction { deleteMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(memoId: String): AppActionChangedCount = runAppAction { restoreMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionChangedCount() }
}
