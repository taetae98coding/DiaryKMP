package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.domain.tag.usecase.AddTagLinkUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveTagLinkUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory

private const val LINKED_TAG_ID = "linkedTagId"

@Factory
public class TagLinkAppAction internal constructor(
    private val addTagLinkUseCase: AddTagLinkUseCase,
    private val removeTagLinkUseCase: RemoveTagLinkUseCase,
) {
    public suspend fun add(
        tagId: String,
        linkedTagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = AddTagLinkUseCase.Parameter(fromTagId = tagId.toUuidArgument(name = TAG_ID), toTagId = linkedTagId.toUuidArgument(name = LINKED_TAG_ID))

            addTagLinkUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }

    public suspend fun remove(
        tagId: String,
        linkedTagId: String,
    ): AppActionDone =
        runAppAction {
            val parameter = RemoveTagLinkUseCase.Parameter(fromTagId = tagId.toUuidArgument(name = TAG_ID), toTagId = linkedTagId.toUuidArgument(name = LINKED_TAG_ID))

            removeTagLinkUseCase(parameter = parameter).getOrThrow()
            appActionDone
        }
}
