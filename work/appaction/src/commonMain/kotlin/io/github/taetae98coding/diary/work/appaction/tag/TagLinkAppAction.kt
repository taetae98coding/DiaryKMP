package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionNotDone
import io.github.taetae98coding.diary.work.appaction.core.isActive
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val LINKED_TAG_ID = "linkedTagId"

@Factory
public class TagLinkAppAction internal constructor(
    private val findTagUseCase: FindTagUseCase,
    private val addTagLinkUseCase: AddTagLinkUseCase,
    private val removeTagLinkUseCase: RemoveTagLinkUseCase,
) {
    public suspend fun add(
        tagId: String,
        linkedTagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = AddTagLinkUseCase.Parameter(fromTagId = tagId.toUuidArgument(name = TAG_ID), toTagId = linkedTagId.toUuidArgument(name = LINKED_TAG_ID))

            if (!isLinkable(tagId = parameter.fromTagId, linkedTagId = parameter.toTagId)) return@runAppAction appActionNotDone

            addTagLinkUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }

    public suspend fun remove(
        tagId: String,
        linkedTagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = RemoveTagLinkUseCase.Parameter(fromTagId = tagId.toUuidArgument(name = TAG_ID), toTagId = linkedTagId.toUuidArgument(name = LINKED_TAG_ID))

            if (!isLinkable(tagId = parameter.fromTagId, linkedTagId = parameter.toTagId)) return@runAppAction appActionNotDone

            removeTagLinkUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }

    private suspend fun isLinkable(
        tagId: Uuid,
        linkedTagId: Uuid,
    ): Boolean = findTagUseCase.isActive(id = tagId, isDeleted = Tag::isDeleted) && findTagUseCase.isActive(id = linkedTagId, isDeleted = Tag::isDeleted)
}
