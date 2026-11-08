package io.github.taetae98coding.diary.work.appaction.memolink

import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val MEMO_ID = "memoId"
private const val TAG_ID = "tagId"
private const val PLACE_ID = "placeId"
private const val WEB_ID = "webId"
private const val CONTACT_ID = "contactId"

@Factory
public class MemoLinkAppAction internal constructor(
    private val addMemoTagUseCase: AddMemoTagUseCase,
    private val removeMemoTagUseCase: RemoveMemoTagUseCase,
    private val setMemoPrimaryTagUseCase: SetMemoPrimaryTagUseCase,
    private val unsetMemoPrimaryTagUseCase: UnsetMemoPrimaryTagUseCase,
    private val addMemoPlaceUseCase: AddMemoPlaceUseCase,
    private val removeMemoPlaceUseCase: RemoveMemoPlaceUseCase,
    private val addMemoWebUseCase: AddMemoWebUseCase,
    private val removeMemoWebUseCase: RemoveMemoWebUseCase,
    private val addMemoContactUseCase: AddMemoContactUseCase,
    private val removeMemoContactUseCase: RemoveMemoContactUseCase,
) {
    public suspend fun tagAdd(
        memoId: String,
        tagId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID) { memo, tag ->
            addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun tagRemove(
        memoId: String,
        tagId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID) { memo, tag ->
            removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun primaryTagSet(
        memoId: String,
        tagId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID) { memo, tag ->
            setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun primaryTagUnset(memoId: String): AppActionDone =
        runAppAction {
            unsetMemoPrimaryTagUseCase(parameter = memoId.toUuidArgument(name = MEMO_ID)).getOrThrow()
            appActionDone
        }

    public suspend fun placeAdd(
        memoId: String,
        placeId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = placeId, targetName = PLACE_ID) { memo, place ->
            addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memo, placeId = place)).getOrThrow()
        }

    public suspend fun placeRemove(
        memoId: String,
        placeId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = placeId, targetName = PLACE_ID) { memo, place ->
            removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memo, placeId = place)).getOrThrow()
        }

    public suspend fun webAdd(
        memoId: String,
        webId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = webId, targetName = WEB_ID) { memo, web ->
            addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = memo, webId = web)).getOrThrow()
        }

    public suspend fun webRemove(
        memoId: String,
        webId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = webId, targetName = WEB_ID) { memo, web ->
            removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memo, webId = web)).getOrThrow()
        }

    public suspend fun contactAdd(
        memoId: String,
        contactId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = contactId, targetName = CONTACT_ID) { memo, contact ->
            addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = memo, contactId = contact)).getOrThrow()
        }

    public suspend fun contactRemove(
        memoId: String,
        contactId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = contactId, targetName = CONTACT_ID) { memo, contact ->
            removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memo, contactId = contact)).getOrThrow()
        }

    private suspend fun link(
        memoId: String,
        targetId: String,
        targetName: String,
        block: suspend (memoId: Uuid, targetId: Uuid) -> Unit,
    ): AppActionDone =
        runAppAction {
            block(memoId.toUuidArgument(name = MEMO_ID), targetId.toUuidArgument(name = targetName))
            appActionDone
        }
}
