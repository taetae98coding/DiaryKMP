package io.github.taetae98coding.diary.work.appaction.memolink

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoPlaceUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoWebUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.UnsetMemoPrimaryTagUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionNotDone
import io.github.taetae98coding.diary.work.appaction.core.isActive
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
    private val findMemoUseCase: FindMemoUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val findContactUseCase: FindContactUseCase,
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
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID, findTargetUseCase = findTagUseCase, isTargetDeleted = Tag::isDeleted) { memo, tag ->
            addMemoTagUseCase(parameter = AddMemoTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun tagRemove(
        memoId: String,
        tagId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID, findTargetUseCase = findTagUseCase, isTargetDeleted = Tag::isDeleted) { memo, tag ->
            removeMemoTagUseCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun primaryTagSet(
        memoId: String,
        tagId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = tagId, targetName = TAG_ID, findTargetUseCase = findTagUseCase, isTargetDeleted = Tag::isDeleted) { memo, tag ->
            setMemoPrimaryTagUseCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memo, tagId = tag)).getOrThrow()
        }

    public suspend fun primaryTagUnset(memoId: String): AppActionDone =
        runAppAction {
            val memo = memoId.toUuidArgument(name = MEMO_ID)

            if (!findMemoUseCase.isActive(id = memo, isDeleted = Memo::isDeleted)) return@runAppAction appActionNotDone

            unsetMemoPrimaryTagUseCase(parameter = memo).getOrThrow()
            appActionDone
        }

    public suspend fun placeAdd(
        memoId: String,
        placeId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = placeId, targetName = PLACE_ID, findTargetUseCase = findPlaceUseCase, isTargetDeleted = Place::isDeleted) { memo, place ->
            addMemoPlaceUseCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memo, placeId = place)).getOrThrow()
        }

    public suspend fun placeRemove(
        memoId: String,
        placeId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = placeId, targetName = PLACE_ID, findTargetUseCase = findPlaceUseCase, isTargetDeleted = Place::isDeleted) { memo, place ->
            removeMemoPlaceUseCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memo, placeId = place)).getOrThrow()
        }

    public suspend fun webAdd(
        memoId: String,
        webId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = webId, targetName = WEB_ID, findTargetUseCase = findWebUseCase, isTargetDeleted = Web::isDeleted) { memo, web ->
            addMemoWebUseCase(parameter = AddMemoWebUseCase.Parameter(memoId = memo, webId = web)).getOrThrow()
        }

    public suspend fun webRemove(
        memoId: String,
        webId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = webId, targetName = WEB_ID, findTargetUseCase = findWebUseCase, isTargetDeleted = Web::isDeleted) { memo, web ->
            removeMemoWebUseCase(parameter = RemoveMemoWebUseCase.Parameter(memoId = memo, webId = web)).getOrThrow()
        }

    public suspend fun contactAdd(
        memoId: String,
        contactId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = contactId, targetName = CONTACT_ID, findTargetUseCase = findContactUseCase, isTargetDeleted = Contact::isDeleted) { memo, contact ->
            addMemoContactUseCase(parameter = AddMemoContactUseCase.Parameter(memoId = memo, contactId = contact)).getOrThrow()
        }

    public suspend fun contactRemove(
        memoId: String,
        contactId: String,
    ): AppActionDone =
        link(memoId = memoId, targetId = contactId, targetName = CONTACT_ID, findTargetUseCase = findContactUseCase, isTargetDeleted = Contact::isDeleted) { memo, contact ->
            removeMemoContactUseCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memo, contactId = contact)).getOrThrow()
        }

    private suspend fun <T : Any> link(
        memoId: String,
        targetId: String,
        targetName: String,
        findTargetUseCase: FlowUseCase<Uuid, T?>,
        isTargetDeleted: (T) -> Boolean,
        block: suspend (memoId: Uuid, targetId: Uuid) -> Unit,
    ): AppActionDone =
        runAppAction {
            val memo = memoId.toUuidArgument(name = MEMO_ID)
            val target = targetId.toUuidArgument(name = targetName)
            val isLinkable = findMemoUseCase.isActive(id = memo, isDeleted = Memo::isDeleted) && findTargetUseCase.isActive(id = target, isDeleted = isTargetDeleted)

            if (!isLinkable) return@runAppAction appActionNotDone

            block(memo, target)
            appActionDone
        }
}
