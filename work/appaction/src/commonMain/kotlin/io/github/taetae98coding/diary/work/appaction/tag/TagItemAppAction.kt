package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.checklist.usecase.PageTagChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageTagContactUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.PageTagDDayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageTagMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.PageTagQrUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.checklist.toAppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.contact.contactSortList
import io.github.taetae98coding.diary.work.appaction.contact.toAppActionContact
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.pageActive
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.toAppActionDDay
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.memoSortList
import io.github.taetae98coding.diary.work.appaction.memo.toAppActionMemo
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.musicSortList
import io.github.taetae98coding.diary.work.appaction.music.toAppActionMusic
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.place.toAppActionPlace
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.toAppActionQr
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.github.taetae98coding.diary.work.appaction.web.toAppActionWeb
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

@Factory
public class TagItemAppAction internal constructor(
    private val findTagUseCase: FindTagUseCase,
    private val pageTagMemoUseCase: PageTagMemoUseCase,
    private val pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    private val pageTagWebUseCase: PageTagWebUseCase,
    private val pageTagPlaceUseCase: PageTagPlaceUseCase,
    private val pageTagContactUseCase: PageTagContactUseCase,
    private val pageTagChecklistUseCase: PageTagChecklistUseCase,
    private val pageTagMusicUseCase: PageTagMusicUseCase,
    private val pageTagQrUseCase: PageTagQrUseCase,
    private val pageTagDDayUseCase: PageTagDDayUseCase,
    private val clock: Clock,
) {
    public suspend fun memoList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageTagMemoUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun memoFinishedList(
        tagId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), sort = sort.toSortArgument(memoSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageFinishedTagMemoUseCase.readPage(parameter = parameter, offset = offset).map { memo -> memo.toAppActionMemo() }
            }
        }

    public suspend fun webList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionWeb> =
        runAppAction {
            val parameter = PageTagWebUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(tagSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagWebUseCase.readPage(parameter = parameter, offset = offset).map { web -> web.toAppActionWeb() }
            }
        }

    public suspend fun placeList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionPlace> =
        runAppAction {
            val parameter = PageTagPlaceUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(tagSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagPlaceUseCase.readPage(parameter = parameter, offset = offset).map { place -> place.toAppActionPlace() }
            }
        }

    public suspend fun contactList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionContact> =
        runAppAction {
            val parameter = PageTagContactUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(contactSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagContactUseCase.readPage(parameter = parameter, offset = offset).map { contact -> contact.toAppActionContact() }
            }
        }

    public suspend fun checklistList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppActionPage<AppActionChecklistSummary> =
        runAppAction {
            val parameter = PageTagChecklistUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument())
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagChecklistUseCase.readPage(parameter = parameter, offset = offset).map { summary -> summary.toAppActionChecklistSummary() }
            }
        }

    public suspend fun musicList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMusic> =
        runAppAction {
            val parameter = PageTagMusicUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(musicSortList))
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagMusicUseCase.readPage(parameter = parameter, offset = offset).map { music -> music.toAppActionMusic() }
            }
        }

    public suspend fun qrList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppActionPage<AppActionQr> =
        runAppAction {
            val parameter = PageTagQrUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument())
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                pageTagQrUseCase.readPage(parameter = parameter, offset = offset).map { qr -> qr.toAppActionQr() }
            }
        }

    public suspend fun ddayList(
        tagId: String,
        scope: String?,
        cursor: String?,
    ): AppActionPage<AppActionDDay> =
        runAppAction {
            val parameter = PageTagDDayUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument())
            val offset = cursor.toPageOffset()

            findTagUseCase.pageActive(id = parameter.tagId, isDeleted = Tag::isDeleted) {
                val today = clock.todayIn(TimeZone.currentSystemDefault())

                pageTagDDayUseCase.readPage(parameter = parameter, offset = offset).map { dday -> dday.toAppActionDDay(today = today) }
            }
        }
}
