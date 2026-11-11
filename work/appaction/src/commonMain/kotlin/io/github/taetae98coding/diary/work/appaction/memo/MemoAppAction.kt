package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.model.memo.Memo
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
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UpdateMemoUseCase
import io.github.taetae98coding.diary.work.appaction.contact.toAppActionContact
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toColorArgumentOrRandom
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.place.toAppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import io.github.taetae98coding.diary.work.appaction.web.toAppActionWeb
import org.koin.core.annotation.Factory

private const val MEMO_ID = "memoId"

@Factory
public class MemoAppAction internal constructor(
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
    public suspend fun get(memoId: String): AppActionMemoDetail? = runAppAction { detail(memoId = memoId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(memoId: String): AppActionMemoDetail? = runAppAction { detail(memoId = memoId, includesDeleted = true) }

    private suspend fun detail(
        memoId: String,
        includesDeleted: Boolean,
    ): AppActionMemoDetail? {
        val id = memoId.toUuidArgument(name = MEMO_ID)
        val memo = findMemoUseCase.readFirst(parameter = id)?.takeIf { memo -> includesDeleted || !memo.isDeleted } ?: return null

        return AppActionMemoDetail(
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
            val current = findMemoUseCase.readActive(id = id, isDeleted = Memo::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
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

    public suspend fun copy(memoId: String): AppActionId =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)

            requireNotNull(findMemoUseCase.readActive(id = id, isDeleted = Memo::isDeleted))
            copyMemoUseCase(parameter = id).getOrThrow().toAppActionId()
        }

    public suspend fun finish(memoId: String): AppActionChangedCount =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)

            findMemoUseCase.changeActive(id = id, isDeleted = Memo::isDeleted) { finishMemoUseCase(parameter = id) }
        }

    public suspend fun restart(memoId: String): AppActionChangedCount =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)

            findMemoUseCase.changeActive(id = id, isDeleted = Memo::isDeleted) { restartMemoUseCase(parameter = id) }
        }

    public suspend fun delete(memoId: String): AppActionChangedCount =
        runAppAction {
            val id = memoId.toUuidArgument(name = MEMO_ID)

            findMemoUseCase.changeActive(id = id, isDeleted = Memo::isDeleted) { deleteMemoUseCase(parameter = id) }
        }

    public suspend fun restore(memoId: String): AppActionChangedCount = runAppAction { restoreMemoUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow().toAppActionChangedCount() }
}
