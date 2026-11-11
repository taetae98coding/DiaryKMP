package io.github.taetae98coding.diary.work.appaction.memo

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedContactMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedPlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedWebMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoHomeUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PagePlaceMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageWebMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.pageActive
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory

private const val PLACE_ID = "placeId"
private const val WEB_ID = "webId"
private const val CONTACT_ID = "contactId"

internal val memoSortList: List<ListSort> = listOf(ListSort.DEFAULT, ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class MemoListAppAction internal constructor(
    private val findPlaceUseCase: FindPlaceUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val findContactUseCase: FindContactUseCase,
    private val pageMemoHomeUseCase: PageMemoHomeUseCase,
    private val pageFinishedMemoUseCase: PageFinishedMemoUseCase,
    private val pagePlaceMemoUseCase: PagePlaceMemoUseCase,
    private val pageFinishedPlaceMemoUseCase: PageFinishedPlaceMemoUseCase,
    private val pageWebMemoUseCase: PageWebMemoUseCase,
    private val pageFinishedWebMemoUseCase: PageFinishedWebMemoUseCase,
    private val pageContactMemoUseCase: PageContactMemoUseCase,
    private val pageFinishedContactMemoUseCase: PageFinishedContactMemoUseCase,
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

    public suspend fun placeList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PagePlaceMemoUseCase.Parameter(placeId = placeId.toUuidArgument(name = PLACE_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findPlaceUseCase.pageActive(id = parameter.placeId, isDeleted = Place::isDeleted) {
                pagePlaceMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun placeFinishedList(
        placeId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageFinishedPlaceMemoUseCase.Parameter(placeId = placeId.toUuidArgument(name = PLACE_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findPlaceUseCase.pageActive(id = parameter.placeId, isDeleted = Place::isDeleted) {
                pageFinishedPlaceMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun webList(
        webId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageWebMemoUseCase.Parameter(webId = webId.toUuidArgument(name = WEB_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findWebUseCase.pageActive(id = parameter.webId, isDeleted = Web::isDeleted) {
                pageWebMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun webFinishedList(
        webId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageFinishedWebMemoUseCase.Parameter(webId = webId.toUuidArgument(name = WEB_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findWebUseCase.pageActive(id = parameter.webId, isDeleted = Web::isDeleted) {
                pageFinishedWebMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun contactList(
        contactId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageContactMemoUseCase.Parameter(contactId = contactId.toUuidArgument(name = CONTACT_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findContactUseCase.pageActive(id = parameter.contactId, isDeleted = Contact::isDeleted) {
                pageContactMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun contactFinishedList(
        contactId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageFinishedContactMemoUseCase.Parameter(contactId = contactId.toUuidArgument(name = CONTACT_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findContactUseCase.pageActive(id = parameter.contactId, isDeleted = Contact::isDeleted) {
                pageFinishedContactMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }
}
