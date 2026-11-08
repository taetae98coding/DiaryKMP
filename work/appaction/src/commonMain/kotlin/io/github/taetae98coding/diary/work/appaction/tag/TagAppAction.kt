package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetLinkedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageFinishedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagHomeUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.UpdateTagUseCase
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
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import org.koin.core.annotation.Factory

internal const val TAG_ID = "tagId"

internal val tagSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class TagAppAction internal constructor(
    private val pageTagHomeUseCase: PageTagHomeUseCase,
    private val pageFinishedTagUseCase: PageFinishedTagUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val getLinkedTagUseCase: GetLinkedTagUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val finishTagUseCase: FinishTagUseCase,
    private val restartTagUseCase: RestartTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val restoreTagUseCase: RestoreTagUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionTag> =
        runAppAction {
            pageTagHomeUseCase
                .readPage(parameter = sort.toSortArgument(tagSortList), offset = cursor.toPageOffset())
                .map { tag -> tag.toAppActionTag() }
        }

    public suspend fun finishedList(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionTag> =
        runAppAction {
            pageFinishedTagUseCase
                .readPage(parameter = sort.toSortArgument(tagSortList), offset = cursor.toPageOffset())
                .map { tag -> tag.toAppActionTag() }
        }

    public suspend fun get(tagId: String): AppActionTagDetail? =
        runAppAction {
            val id = tagId.toUuidArgument(name = TAG_ID)
            val tag = findTagUseCase.readFirst(parameter = id) ?: return@runAppAction null

            AppActionTagDetail(
                tag = tag.toAppActionTag(),
                linkedTags = getLinkedTagUseCase.readFirst(parameter = id).map { linkedTag -> linkedTag.toAppActionTag() },
            )
        }

    public suspend fun add(
        title: String,
        emoji: String?,
        description: String?,
        color: String?,
        linkedTagIds: List<String>,
    ): AppActionId =
        runAppAction {
            val parameter =
                AddTagUseCase.Parameter(
                    detail =
                        TagDetail(
                            emoji = emoji.orEmpty(),
                            title = title,
                            description = description.orEmpty(),
                            color = color.toColorArgumentOrRandom(),
                        ),
                    linkedTagIdSet = linkedTagIds.toUuidSetArgument(name = "linkedTagIds"),
                )

            addTagUseCase(parameter = parameter).getOrThrow().toAppActionId()
        }

    public suspend fun update(
        tagId: String,
        emoji: String?,
        title: String?,
        description: String?,
        color: String?,
    ): AppActionChangedCount =
        runAppAction {
            val id = tagId.toUuidArgument(name = TAG_ID)
            val current = findTagUseCase.readFirst(parameter = id)?.detail ?: return@runAppAction 0.toAppActionChangedCount()
            val detail =
                TagDetail(
                    emoji = emoji ?: current.emoji,
                    title = title ?: current.title,
                    description = description ?: current.description,
                    color = color.toColorArgumentOrNull() ?: current.color,
                )

            updateTagUseCase(parameter = UpdateTagUseCase.Parameter(id = id, detail = detail)).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun finish(tagId: String): AppActionChangedCount = runAppAction { finishTagUseCase(parameter = tagId.toUuidArgument(name = TAG_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restart(tagId: String): AppActionChangedCount = runAppAction { restartTagUseCase(parameter = tagId.toUuidArgument(name = TAG_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun delete(tagId: String): AppActionChangedCount = runAppAction { deleteTagUseCase(parameter = tagId.toUuidArgument(name = TAG_ID)).getOrThrow().toAppActionChangedCount() }

    public suspend fun restore(tagId: String): AppActionChangedCount = runAppAction { restoreTagUseCase(parameter = tagId.toUuidArgument(name = TAG_ID)).getOrThrow().toAppActionChangedCount() }
}
