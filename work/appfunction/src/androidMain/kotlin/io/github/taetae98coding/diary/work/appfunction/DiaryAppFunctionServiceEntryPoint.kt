package io.github.taetae98coding.diary.work.appfunction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.AppFunctionStringValueConstraint
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItemSortValue
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactBirthdayCalendarValue
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDayOverflowPolicyValue
import io.github.taetae98coding.diary.work.appaction.dday.DDayAppAction
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction
import io.github.taetae98coding.diary.work.appaction.routine.AppActionRoutineCycleValue
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagScopeValue
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction
import io.github.taetae98coding.diary.work.appfunction.calendar.AppFunctionCalendarBirthday
import io.github.taetae98coding.diary.work.appfunction.calendar.AppFunctionCalendarDDay
import io.github.taetae98coding.diary.work.appfunction.calendar.AppFunctionCalendarMemo
import io.github.taetae98coding.diary.work.appfunction.calendar.AppFunctionHoliday
import io.github.taetae98coding.diary.work.appfunction.calendar.toAppFunctionCalendarBirthday
import io.github.taetae98coding.diary.work.appfunction.calendar.toAppFunctionCalendarDDay
import io.github.taetae98coding.diary.work.appfunction.calendar.toAppFunctionCalendarMemo
import io.github.taetae98coding.diary.work.appfunction.calendar.toAppFunctionHoliday
import io.github.taetae98coding.diary.work.appfunction.checklist.AppFunctionChecklistDetail
import io.github.taetae98coding.diary.work.appfunction.checklist.AppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appfunction.checklist.toAppFunctionChecklistDetail
import io.github.taetae98coding.diary.work.appfunction.checklist.toAppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appfunction.contact.AppFunctionContactDetailWithTags
import io.github.taetae98coding.diary.work.appfunction.contact.AppFunctionContactPage
import io.github.taetae98coding.diary.work.appfunction.contact.toAppFunctionContactDetailWithTags
import io.github.taetae98coding.diary.work.appfunction.contact.toAppFunctionContactPage
import io.github.taetae98coding.diary.work.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appfunction.core.toAppFunctionDone
import io.github.taetae98coding.diary.work.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appfunction.dday.AppFunctionDDayDetail
import io.github.taetae98coding.diary.work.appfunction.dday.AppFunctionDDayPage
import io.github.taetae98coding.diary.work.appfunction.dday.toAppFunctionDDayDetail
import io.github.taetae98coding.diary.work.appfunction.dday.toAppFunctionDDayPage
import io.github.taetae98coding.diary.work.appfunction.lunar.AppFunctionLunarDate
import io.github.taetae98coding.diary.work.appfunction.lunar.toAppFunctionLunarDate
import io.github.taetae98coding.diary.work.appfunction.memo.AppFunctionMemoDateTimeInput
import io.github.taetae98coding.diary.work.appfunction.memo.AppFunctionMemoDetail
import io.github.taetae98coding.diary.work.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appfunction.memo.toAppActionMemoDateTimeInput
import io.github.taetae98coding.diary.work.appfunction.memo.toAppFunctionMemoDetail
import io.github.taetae98coding.diary.work.appfunction.memo.toAppFunctionMemoPage
import io.github.taetae98coding.diary.work.appfunction.music.AppFunctionMusicDetail
import io.github.taetae98coding.diary.work.appfunction.music.AppFunctionMusicPage
import io.github.taetae98coding.diary.work.appfunction.music.toAppFunctionMusicDetail
import io.github.taetae98coding.diary.work.appfunction.music.toAppFunctionMusicPage
import io.github.taetae98coding.diary.work.appfunction.place.AppFunctionPlaceDetail
import io.github.taetae98coding.diary.work.appfunction.place.AppFunctionPlaceList
import io.github.taetae98coding.diary.work.appfunction.place.AppFunctionPlacePage
import io.github.taetae98coding.diary.work.appfunction.place.toAppFunctionPlaceDetail
import io.github.taetae98coding.diary.work.appfunction.place.toAppFunctionPlaceList
import io.github.taetae98coding.diary.work.appfunction.place.toAppFunctionPlacePage
import io.github.taetae98coding.diary.work.appfunction.qr.AppFunctionQrDetail
import io.github.taetae98coding.diary.work.appfunction.qr.AppFunctionQrPage
import io.github.taetae98coding.diary.work.appfunction.qr.toAppFunctionQrDetail
import io.github.taetae98coding.diary.work.appfunction.qr.toAppFunctionQrPage
import io.github.taetae98coding.diary.work.appfunction.routine.AppFunctionRoutineDetail
import io.github.taetae98coding.diary.work.appfunction.routine.AppFunctionRoutinePage
import io.github.taetae98coding.diary.work.appfunction.routine.AppFunctionRoutineRecordPage
import io.github.taetae98coding.diary.work.appfunction.routine.toAppFunctionRoutineDetail
import io.github.taetae98coding.diary.work.appfunction.routine.toAppFunctionRoutinePage
import io.github.taetae98coding.diary.work.appfunction.routine.toAppFunctionRoutineRecordPage
import io.github.taetae98coding.diary.work.appfunction.search.AppFunctionSearchDDayPage
import io.github.taetae98coding.diary.work.appfunction.search.AppFunctionSearchPlacePage
import io.github.taetae98coding.diary.work.appfunction.search.AppFunctionSearchRoutinePage
import io.github.taetae98coding.diary.work.appfunction.search.AppFunctionSearchTagPage
import io.github.taetae98coding.diary.work.appfunction.search.AppFunctionSearchWebPage
import io.github.taetae98coding.diary.work.appfunction.search.toAppFunctionSearchDDayPage
import io.github.taetae98coding.diary.work.appfunction.search.toAppFunctionSearchPlacePage
import io.github.taetae98coding.diary.work.appfunction.search.toAppFunctionSearchRoutinePage
import io.github.taetae98coding.diary.work.appfunction.search.toAppFunctionSearchTagPage
import io.github.taetae98coding.diary.work.appfunction.search.toAppFunctionSearchWebPage
import io.github.taetae98coding.diary.work.appfunction.tag.AppFunctionTagDetail
import io.github.taetae98coding.diary.work.appfunction.tag.AppFunctionTagPage
import io.github.taetae98coding.diary.work.appfunction.tag.AppFunctionTagPlacePage
import io.github.taetae98coding.diary.work.appfunction.tag.AppFunctionTagWebPage
import io.github.taetae98coding.diary.work.appfunction.tag.toAppFunctionTagDetail
import io.github.taetae98coding.diary.work.appfunction.tag.toAppFunctionTagPage
import io.github.taetae98coding.diary.work.appfunction.tag.toAppFunctionTagPlacePage
import io.github.taetae98coding.diary.work.appfunction.tag.toAppFunctionTagWebPage
import io.github.taetae98coding.diary.work.appfunction.web.AppFunctionWebDetail
import io.github.taetae98coding.diary.work.appfunction.web.AppFunctionWebPage
import io.github.taetae98coding.diary.work.appfunction.web.toAppFunctionWebDetail
import io.github.taetae98coding.diary.work.appfunction.web.toAppFunctionWebPage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// AppFunction 컴파일러는 이 클래스에 직접 선언한 함수만 읽고, Android 16은 앱마다 서비스 하나만 색인하므로 모든 도메인의 함수를 여기에 둔다.
// 함수 본문은 공통 XxxAppAction을 부르고 appFunctionCall로 오류를, toAppFunctionXxx로 결과를 AppFunction 타입으로 바꾸기만 하고, 도메인마다 region 하나를 쓴다.
// Koin은 서비스보다 먼저 만들어지는 androidx.startup 제공자가 시작하므로, 화면 없이 프로세스가 시작되어도 여기서는 이미 준비되어 있다.
@Suppress("TooManyFunctions", "LargeClass")
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AppFunctionServiceEntryPoint(
    serviceName = "DiaryAppFunctionService",
    appFunctionXmlFileName = "diary_app_functions",
)
public abstract class DiaryAppFunctionServiceEntryPoint :
    AppFunctionService(),
    KoinComponent {
    private val memoAppAction: MemoAppAction by lazy { get() }
    private val calendarAppAction: CalendarAppAction by lazy { get() }
    private val checklistAppAction: ChecklistAppAction by lazy { get() }
    private val checklistItemAppAction: ChecklistItemAppAction by lazy { get() }
    private val contactAppAction: ContactAppAction by lazy { get() }
    private val ddayAppAction: DDayAppAction by lazy { get() }
    private val entityTagAppAction: EntityTagAppAction by lazy { get() }
    private val lunarAppAction: LunarAppAction by lazy { get() }
    private val memoLinkAppAction: MemoLinkAppAction by lazy { get() }
    private val memoListAppAction: MemoListAppAction by lazy { get() }
    private val musicAppAction: MusicAppAction by lazy { get() }
    private val placeAppAction: PlaceAppAction by lazy { get() }
    private val qrAppAction: QrAppAction by lazy { get() }
    private val routineAppAction: RoutineAppAction by lazy { get() }
    private val routineListAppAction: RoutineListAppAction by lazy { get() }
    private val routineRecordAppAction: RoutineRecordAppAction by lazy { get() }
    private val searchAppAction: SearchAppAction by lazy { get() }
    private val tagAppAction: TagAppAction by lazy { get() }
    private val tagItemAppAction: TagItemAppAction by lazy { get() }
    private val tagLinkAppAction: TagLinkAppAction by lazy { get() }
    private val webAppAction: WebAppAction by lazy { get() }

    // region Memo

    /**
     * Lists the memos shown on the memo home list: memos that are neither finished nor deleted, filtered by the filters the user chose in the app.
     *
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.list(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists finished memos that are not deleted.
     *
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoFinishedList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.finishedList(sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Gets one memo with its linked tags, places, webs and contacts. Returns null when the memo does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @return The memo with its links, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoGet(memoId: String): AppFunctionMemoDetail? = appFunctionCall { memoAppAction.get(memoId = memoId)?.toAppFunctionMemoDetail() }

    /**
     * Adds a memo. The title must not be blank.
     *
     * @param title Title of the memo.
     * @param description Description of the memo.
     * @param color Color of the memo. A random color is used when omitted. Use a hex color like #4285F4.
     * @param dateTime Optional period of the memo.
     * @param primaryTagId Id of the primary tag. It is linked too.
     * @param tagIds Ids of tags to link.
     * @param placeIds Ids of places to link.
     * @param webIds Ids of webs to link.
     * @param contactIds Ids of contacts to link.
     * @return Id of the added memo.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoAdd(
        title: String,
        description: String? = null,
        color: String? = null,
        dateTime: AppFunctionMemoDateTimeInput? = null,
        primaryTagId: String? = null,
        tagIds: List<String> = emptyList(),
        placeIds: List<String> = emptyList(),
        webIds: List<String> = emptyList(),
        contactIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            memoAppAction
                .add(
                    title = title,
                    description = description,
                    color = color,
                    dateTime = dateTime?.toAppActionMemoDateTimeInput(),
                    primaryTagId = primaryTagId,
                    tagIds = tagIds,
                    placeIds = placeIds,
                    webIds = webIds,
                    contactIds = contactIds,
                ).toAppFunctionId()
        }

    /**
     * Updates the title, description, color or period of a memo. Omitted fields keep their current value. A blank title keeps the current title. Links are changed with the memo link functions.
     *
     * @param memoId Id of the memo.
     * @param title New title.
     * @param description New description.
     * @param color New color. Use a hex color like #4285F4.
     * @param dateTime New period.
     * @param clearDateTime Set true to remove the period.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoUpdate(
        memoId: String,
        title: String? = null,
        description: String? = null,
        color: String? = null,
        dateTime: AppFunctionMemoDateTimeInput? = null,
        clearDateTime: Boolean? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            memoAppAction
                .update(
                    memoId = memoId,
                    title = title,
                    description = description,
                    color = color,
                    dateTime = dateTime?.toAppActionMemoDateTimeInput(),
                    clearDateTime = clearDateTime,
                ).toAppFunctionChangedCount()
        }

    /**
     * Copies a memo with its links into a new unfinished memo.
     *
     * @param memoId Id of the memo.
     * @return Id of the new memo.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoCopy(memoId: String): AppFunctionId = appFunctionCall { memoAppAction.copy(memoId = memoId).toAppFunctionId() }

    /**
     * Marks a memo as finished.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoFinish(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.finish(memoId = memoId).toAppFunctionChangedCount() }

    /**
     * Marks a finished memo as not finished.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoRestart(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.restart(memoId = memoId).toAppFunctionChangedCount() }

    /**
     * Deletes a memo. Use memoRestore with the same id to undo.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoDelete(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.delete(memoId = memoId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted memo.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoRestore(memoId: String): AppFunctionChangedCount = appFunctionCall { memoAppAction.restore(memoId = memoId).toAppFunctionChangedCount() }

    // endregion

    // region MemoLink

    /**
     * Links a tag to a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoTagAdd(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.tagAdd(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a memo. If it is the primary tag, the primary tag is cleared too. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoTagRemove(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.tagRemove(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    /**
     * Sets the primary tag of a memo. The tag is linked too when it is not linked yet. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPrimaryTagSet(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.primaryTagSet(memoId = memoId, tagId = tagId).toAppFunctionDone() }

    /**
     * Clears the primary tag of a memo. The tag stays linked. Returns done false and changes nothing when the memo does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPrimaryTagUnset(memoId: String): AppFunctionDone = appFunctionCall { memoLinkAppAction.primaryTagUnset(memoId = memoId).toAppFunctionDone() }

    /**
     * Links a place to a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param placeId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPlaceAdd(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.placeAdd(memoId = memoId, placeId = placeId).toAppFunctionDone() }

    /**
     * Unlinks a place from a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param placeId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPlaceRemove(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.placeRemove(memoId = memoId, placeId = placeId).toAppFunctionDone() }

    /**
     * Links a web to a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param webId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoWebAdd(
        memoId: String,
        webId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.webAdd(memoId = memoId, webId = webId).toAppFunctionDone() }

    /**
     * Unlinks a web from a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param webId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoWebRemove(
        memoId: String,
        webId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.webRemove(memoId = memoId, webId = webId).toAppFunctionDone() }

    /**
     * Links a contact to a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param contactId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoContactAdd(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.contactAdd(memoId = memoId, contactId = contactId).toAppFunctionDone() }

    /**
     * Unlinks a contact from a memo. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param memoId Id of the memo.
     * @param contactId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoContactRemove(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = appFunctionCall { memoLinkAppAction.contactRemove(memoId = memoId, contactId = contactId).toAppFunctionDone() }

    // endregion

    // region Tag

    /**
     * Lists the tags shown on the tag home list: tags that are neither finished nor deleted. When the user turned on the top-level-only filter in the app, only tags that no other tag links to are listed.
     *
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of tags.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionTagPage = appFunctionCall { tagAppAction.list(sort = sort, cursor = cursor).toAppFunctionTagPage() }

    /**
     * Lists finished tags that are not deleted.
     *
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of tags.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagFinishedList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionTagPage = appFunctionCall { tagAppAction.finishedList(sort = sort, cursor = cursor).toAppFunctionTagPage() }

    /**
     * Gets one tag with the tags it links to (its child tags). Returns null when the tag does not exist or is deleted. Use the tag item list functions such as tagMemoList and tagWebList for the items linked to the tag.
     *
     * @param tagId Id of the tag.
     * @return The tag with its child tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagGet(tagId: String): AppFunctionTagDetail? = appFunctionCall { tagAppAction.get(tagId = tagId)?.toAppFunctionTagDetail() }

    /**
     * Adds a tag. The title must not be blank.
     *
     * @param title Title of the tag.
     * @param emoji A single emoji shown before the title.
     * @param description Description of the tag.
     * @param color Color of the tag. A random color is used when omitted. Use a hex color like #4285F4.
     * @param linkedTagIds Ids of tags the new tag links to as its child tags.
     * @return Id of the added tag.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagAdd(
        title: String,
        emoji: String? = null,
        description: String? = null,
        color: String? = null,
        linkedTagIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            tagAppAction.add(title = title, emoji = emoji, description = description, color = color, linkedTagIds = linkedTagIds).toAppFunctionId()
        }

    /**
     * Updates the emoji, title, description or color of a tag. Omitted fields keep their current value. A blank title keeps the current title. Set emoji to an empty string to remove it. Links are changed with tagLinkAdd and tagLinkRemove.
     *
     * @param tagId Id of the tag.
     * @param emoji New emoji.
     * @param title New title.
     * @param description New description.
     * @param color New color. Use a hex color like #4285F4.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagUpdate(
        tagId: String,
        emoji: String? = null,
        title: String? = null,
        description: String? = null,
        color: String? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            tagAppAction.update(tagId = tagId, emoji = emoji, title = title, description = description, color = color).toAppFunctionChangedCount()
        }

    /**
     * Marks a tag as finished.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagFinish(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.finish(tagId = tagId).toAppFunctionChangedCount() }

    /**
     * Marks a finished tag as not finished.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRestart(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.restart(tagId = tagId).toAppFunctionChangedCount() }

    /**
     * Deletes a tag. Use tagRestore with the same id to undo.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagDelete(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.delete(tagId = tagId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted tag.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRestore(tagId: String): AppFunctionChangedCount = appFunctionCall { tagAppAction.restore(tagId = tagId).toAppFunctionChangedCount() }

    // endregion

    // region TagLink

    /**
     * Links a tag to another tag, making the linked tag a child of the tag. Links are one-way and a tag cannot link to itself. Linking does not change the tags of memos, webs or places. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param tagId Id of the tag.
     * @param linkedTagId Id of the tag that the tag links to (the child tag).
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagLinkAdd(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = appFunctionCall { tagLinkAppAction.add(tagId = tagId, linkedTagId = linkedTagId).toAppFunctionDone() }

    /**
     * Removes the link from a tag to another tag. The link in the opposite direction is kept. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param tagId Id of the tag.
     * @param linkedTagId Id of the tag that the tag links to (the child tag).
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagLinkRemove(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = appFunctionCall { tagLinkAppAction.remove(tagId = tagId, linkedTagId = linkedTagId).toAppFunctionDone() }

    // endregion

    // region TagItem

    /**
     * Lists memos linked to a tag that are neither finished nor deleted. A memo linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagMemoList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { tagItemAppAction.memoList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists finished memos linked to a tag that are not deleted. Only memos linked to this tag itself are included.
     *
     * @param tagId Id of the tag.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagMemoFinishedList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { tagItemAppAction.memoFinishedList(tagId = tagId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists webs linked to a tag that are not deleted. A web linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of webs.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagWebList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionTagWebPage = appFunctionCall { tagItemAppAction.webList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionTagWebPage() }

    /**
     * Lists places linked to a tag that are not deleted. A place linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of places.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagPlaceList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionTagPlacePage = appFunctionCall { tagItemAppAction.placeList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionTagPlacePage() }

    /**
     * Lists contacts linked to a tag that are not deleted. A contact linked to several included tags appears once. Favorite contacts always come first, and the sort applies within favorites and within the rest.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param sort Sort order. Omit it to use the list's initial order (name).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of contacts.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagContactList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.NAME, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionContactPage = appFunctionCall { tagItemAppAction.contactList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionContactPage() }

    /**
     * Lists checklists linked to a tag that are not deleted, by title, with how many of their items are checked. A checklist linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of checklists.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagChecklistList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        cursor: String? = null,
    ): AppFunctionChecklistSummaryPage = appFunctionCall { tagItemAppAction.checklistList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionChecklistSummaryPage() }

    /**
     * Lists the music linked to a tag that is not deleted. Music linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of music.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagMusicList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMusicPage = appFunctionCall { tagItemAppAction.musicList(tagId = tagId, scope = scope, sort = sort, cursor = cursor).toAppFunctionMusicPage() }

    /**
     * Lists QR codes linked to a tag that are not deleted, ordered by title. A QR code linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of QR codes.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagQrList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        cursor: String? = null,
    ): AppFunctionQrPage = appFunctionCall { tagItemAppAction.qrList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionQrPage() }

    /**
     * Lists D-Days linked to a tag that are not deleted, by title, with the days left or passed counted from today. A D-Day linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of D-Days.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagDdayList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        cursor: String? = null,
    ): AppFunctionDDayPage = appFunctionCall { tagItemAppAction.ddayList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionDDayPage() }

    /**
     * Lists routines linked to a tag that are not finished and not deleted, in progress first, then not started, by title within each, with this cycle's total and achievement rate counted from today. A routine linked to several included tags appears once.
     *
     * @param tagId Id of the tag.
     * @param scope Which tags to include. self: only this tag. child: this tag and the tags it links to directly. descendant: this tag and every tag reachable through its links. Omit it to use self.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of routines.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRoutineList(
        tagId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionTagScopeValue.SELF, AppActionTagScopeValue.CHILD, AppActionTagScopeValue.DESCENDANT])
        scope: String? = null,
        cursor: String? = null,
    ): AppFunctionRoutinePage = appFunctionCall { tagItemAppAction.routineList(tagId = tagId, scope = scope, cursor = cursor).toAppFunctionRoutinePage() }

    /**
     * Lists finished routines linked to a tag that are not deleted, in progress first, then not started, by title within each. Only routines linked to this tag itself are included.
     *
     * @param tagId Id of the tag.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of routines.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRoutineFinishedList(
        tagId: String,
        cursor: String? = null,
    ): AppFunctionRoutinePage = appFunctionCall { tagItemAppAction.routineFinishedList(tagId = tagId, cursor = cursor).toAppFunctionRoutinePage() }

    // endregion

    // region Place

    /**
     * Lists all places that are not deleted, as on the place home list.
     *
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of places.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionPlacePage = appFunctionCall { placeAppAction.list(sort = sort, cursor = cursor).toAppFunctionPlacePage() }

    /**
     * Lists the places that are not deleted and lie inside a latitude and longitude area, as on the place home map. The whole result is returned at once.
     *
     * @param south Southern latitude of the area. Use a decimal number like 37.566500.
     * @param north Northern latitude of the area. Use a decimal number like 37.566500.
     * @param west Western longitude of the area. It may be greater than east when the area crosses the 180th meridian. Use a decimal number like 37.566500.
     * @param east Eastern longitude of the area. Use a decimal number like 37.566500.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @return All places inside the area.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeListInArea(
        south: String,
        north: String,
        west: String,
        east: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
    ): AppFunctionPlaceList = appFunctionCall { placeAppAction.listInArea(south = south, north = north, west = west, east = east, sort = sort).toAppFunctionPlaceList() }

    /**
     * Gets one place with its linked tags. Returns null when the place does not exist or is deleted.
     *
     * @param placeId Id of the place.
     * @return The place with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeGet(placeId: String): AppFunctionPlaceDetail? = appFunctionCall { placeAppAction.get(placeId = placeId)?.toAppFunctionPlaceDetail() }

    /**
     * Lists the memos linked to a place that are neither finished nor deleted.
     *
     * @param placeId Id of the place.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeMemoList(
        placeId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.placeList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists the finished memos linked to a place that are not deleted.
     *
     * @param placeId Id of the place.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeMemoFinishedList(
        placeId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.placeFinishedList(placeId = placeId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Adds a place. The title must not be blank and latitude and longitude must be within range. Coordinates and address are not looked up, so pass the values you already know.
     *
     * @param title Title of the place.
     * @param latitude Latitude between -90 and 90. Use a decimal number like 37.566500.
     * @param longitude Longitude between -180 and 180. Use a decimal number like 37.566500.
     * @param address Address of the place.
     * @param description Description of the place.
     * @param color Color of the place. A random color is used when omitted. Use a hex color like #4285F4.
     * @param tagIds Ids of tags to link.
     * @return Id of the added place.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeAdd(
        title: String,
        latitude: String,
        longitude: String,
        address: String? = null,
        description: String? = null,
        color: String? = null,
        tagIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            placeAppAction
                .add(
                    title = title,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    description = description,
                    color = color,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    /**
     * Updates the title, description, color, address or coordinates of a place. Omitted fields keep their current value. A blank title keeps the current title. Tags are changed with placeTagAdd and placeTagRemove.
     *
     * @param placeId Id of the place.
     * @param title New title.
     * @param description New description.
     * @param color New color. Use a hex color like #4285F4.
     * @param address New address.
     * @param latitude New latitude between -90 and 90. Use a decimal number like 37.566500.
     * @param longitude New longitude between -180 and 180. Use a decimal number like 37.566500.
     * @return How many places were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeUpdate(
        placeId: String,
        title: String? = null,
        description: String? = null,
        color: String? = null,
        address: String? = null,
        latitude: String? = null,
        longitude: String? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            placeAppAction
                .update(
                    placeId = placeId,
                    title = title,
                    description = description,
                    color = color,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                ).toAppFunctionChangedCount()
        }

    /**
     * Deletes a place. Links to memos are kept. Use placeRestore with the same id to undo.
     *
     * @param placeId Id of the place.
     * @return How many places were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeDelete(placeId: String): AppFunctionChangedCount = appFunctionCall { placeAppAction.delete(placeId = placeId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted place.
     *
     * @param placeId Id of the place.
     * @return How many places were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeRestore(placeId: String): AppFunctionChangedCount = appFunctionCall { placeAppAction.restore(placeId = placeId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a place. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param placeId Id of the place.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeTagAdd(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.PLACE, entityId = placeId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a place. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param placeId Id of the place.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeTagRemove(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.PLACE, entityId = placeId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Web

    /**
     * Lists all webs (saved web pages) that are not deleted, as on the web home list.
     *
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of webs.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionWebPage = appFunctionCall { webAppAction.list(sort = sort, cursor = cursor).toAppFunctionWebPage() }

    /**
     * Gets one web with its request headers and linked tags. Returns null when the web does not exist or is deleted.
     *
     * @param webId Id of the web.
     * @return The web with its request headers and tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webGet(webId: String): AppFunctionWebDetail? = appFunctionCall { webAppAction.get(webId = webId)?.toAppFunctionWebDetail() }

    /**
     * Lists the memos linked to a web that are neither finished nor deleted.
     *
     * @param webId Id of the web.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webMemoList(
        webId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.webList(webId = webId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists the finished memos linked to a web that are not deleted.
     *
     * @param webId Id of the web.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webMemoFinishedList(
        webId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.webFinishedList(webId = webId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Adds a web. The title and url must not be blank and every header needs a name. The page is not fetched, so pass the title you already know.
     *
     * @param title Title of the web.
     * @param url Url of the web page.
     * @param description Description of the web.
     * @param headers Request headers sent when the page is opened in the app. Each item is "Name: value", split at the first colon. Order is kept and duplicate names are allowed.
     * @param tagIds Ids of tags to link.
     * @return Id of the added web.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webAdd(
        title: String,
        url: String,
        description: String? = null,
        headers: List<String> = emptyList(),
        tagIds: List<String> = emptyList(),
    ): AppFunctionId = appFunctionCall { webAppAction.add(title = title, url = url, description = description, headers = headers, tagIds = tagIds).toAppFunctionId() }

    /**
     * Updates the title, description, url or request headers of a web. Omitted fields keep their current value. A blank title or url keeps the current value. Tags are changed with webTagAdd and webTagRemove.
     *
     * @param webId Id of the web.
     * @param title New title.
     * @param description New description.
     * @param url New url.
     * @param headers New request headers. They replace all current headers; pass an empty list to remove them. Each item is "Name: value", split at the first colon. Order is kept and duplicate names are allowed.
     * @return How many webs were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webUpdate(
        webId: String,
        title: String? = null,
        description: String? = null,
        url: String? = null,
        headers: List<String>? = null,
    ): AppFunctionChangedCount = appFunctionCall { webAppAction.update(webId = webId, title = title, description = description, url = url, headers = headers).toAppFunctionChangedCount() }

    /**
     * Deletes a web. Links to memos are kept. Use webRestore with the same id to undo.
     *
     * @param webId Id of the web.
     * @return How many webs were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webDelete(webId: String): AppFunctionChangedCount = appFunctionCall { webAppAction.delete(webId = webId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted web.
     *
     * @param webId Id of the web.
     * @return How many webs were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webRestore(webId: String): AppFunctionChangedCount = appFunctionCall { webAppAction.restore(webId = webId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a web. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param webId Id of the web.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webTagAdd(
        webId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.WEB, entityId = webId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a web. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param webId Id of the web.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webTagRemove(
        webId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.WEB, entityId = webId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Contact

    /**
     * Lists contacts that are not deleted. Favorite contacts always come first, and the sort applies within favorites and within the rest.
     *
     * @param sort Sort order. Omit it to use the list's initial order (name).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of contacts.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.NAME, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionContactPage = appFunctionCall { contactAppAction.list(sort = sort, cursor = cursor).toAppFunctionContactPage() }

    /**
     * Gets one contact with all its details and linked tags. Returns null when the contact does not exist or is deleted.
     *
     * @param contactId Id of the contact.
     * @return The contact with all its details and its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactGet(contactId: String): AppFunctionContactDetailWithTags? = appFunctionCall { contactAppAction.get(contactId = contactId)?.toAppFunctionContactDetailWithTags() }

    /**
     * Lists unfinished memos linked to a contact that are not deleted.
     *
     * @param contactId Id of the contact.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactMemoList(
        contactId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.contactList(contactId = contactId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Lists finished memos linked to a contact that are not deleted.
     *
     * @param contactId Id of the contact.
     * @param sort Sort order. Omit it to use the list's initial order (default).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactMemoFinishedList(
        contactId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.DEFAULT, AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { memoListAppAction.contactFinishedList(contactId = contactId, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Adds a contact. The name must not be blank and no phone number may be blank. The new contact is not a favorite.
     *
     * @param name Name of the contact.
     * @param description Description of the contact.
     * @param heightCm Height in centimeters as a string with up to three digits and at most one decimal place, like "172.5".
     * @param footSizeMm Shoe size in whole millimeters from 0 to 999, like 270.
     * @param birthday Birthday as a date like 1990-12-20. The date is read in the calendar of 'birthdayCalendar', so a lunar birthday is the lunar year, month and day as is. It also sets the birth year to its year.
     * @param birthdayCalendar Calendar of the birthday. Only valid with a birthday.
     * @param birthYear Birth year from 0 to 9999 for when only the year is known. Ignored while the contact has a birthday, whose year is used instead.
     * @param hometown Hometown as one line of free text.
     * @param howWeMet How the user met the person, as one line of free text.
     * @param phoneNumbers Phone numbers in order. Each number must not be blank. The format is not checked.
     * @param tagIds Ids of tags to link.
     * @return Id of the added contact.
     */
    @Suppress("LongParameterList")
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactAdd(
        name: String,
        description: String? = null,
        heightCm: String? = null,
        footSizeMm: Int? = null,
        birthday: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionContactBirthdayCalendarValue.SOLAR, AppActionContactBirthdayCalendarValue.LUNAR])
        birthdayCalendar: String? = null,
        birthYear: Int? = null,
        hometown: String? = null,
        howWeMet: String? = null,
        phoneNumbers: List<String> = emptyList(),
        tagIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            contactAppAction
                .add(
                    name = name,
                    description = description,
                    heightCm = heightCm,
                    footSizeMm = footSizeMm,
                    birthday = birthday,
                    birthdayCalendar = birthdayCalendar,
                    birthYear = birthYear,
                    hometown = hometown,
                    howWeMet = howWeMet,
                    phoneNumbers = phoneNumbers,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    /**
     * Updates the details of a contact. Omitted fields keep their current value. A blank name keeps the current name. Changing only the birthday date keeps its calendar.
     * Removing the birthday also removes the birth year unless a birth year is given. Favorite is changed with contactFavorite and contactUnfavorite. Tags are changed with contactTagAdd and contactTagRemove.
     *
     * @param contactId Id of the contact.
     * @param name New name.
     * @param description New description.
     * @param heightCm Height in centimeters as a string with up to three digits and at most one decimal place, like "172.5".
     * @param clearHeight Set true to remove the height.
     * @param footSizeMm Shoe size in whole millimeters from 0 to 999, like 270.
     * @param clearFootSize Set true to remove the shoe size.
     * @param birthday Birthday as a date like 1990-12-20. The date is read in the calendar of 'birthdayCalendar', so a lunar birthday is the lunar year, month and day as is. It also sets the birth year to its year.
     * @param birthdayCalendar Calendar of the birthday. Only valid with a birthday.
     * @param clearBirthday Set true to remove the birthday.
     * @param birthYear Birth year from 0 to 9999 for when only the year is known. Ignored while the contact has a birthday, whose year is used instead.
     * @param clearBirthYear Set true to remove the birth year of a contact without a birthday.
     * @param hometown New hometown.
     * @param howWeMet New text about how the user met the person.
     * @param phoneNumbers Replaces all phone numbers, in order. Pass an empty list to remove them all. Each number must not be blank.
     * @return How many contacts were changed.
     */
    @Suppress("LongParameterList")
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactUpdate(
        contactId: String,
        name: String? = null,
        description: String? = null,
        heightCm: String? = null,
        clearHeight: Boolean? = null,
        footSizeMm: Int? = null,
        clearFootSize: Boolean? = null,
        birthday: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionContactBirthdayCalendarValue.SOLAR, AppActionContactBirthdayCalendarValue.LUNAR])
        birthdayCalendar: String? = null,
        clearBirthday: Boolean? = null,
        birthYear: Int? = null,
        clearBirthYear: Boolean? = null,
        hometown: String? = null,
        howWeMet: String? = null,
        phoneNumbers: List<String>? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            contactAppAction
                .update(
                    input =
                        AppActionContactUpdateInput(
                            contactId = contactId,
                            name = name,
                            description = description,
                            heightCm = heightCm,
                            clearHeight = clearHeight,
                            footSizeMm = footSizeMm,
                            clearFootSize = clearFootSize,
                            birthday = birthday,
                            birthdayCalendar = birthdayCalendar,
                            clearBirthday = clearBirthday,
                            birthYear = birthYear,
                            clearBirthYear = clearBirthYear,
                            hometown = hometown,
                            howWeMet = howWeMet,
                            phoneNumbers = phoneNumbers,
                        ),
                ).toAppFunctionChangedCount()
        }

    /**
     * Marks a contact as a favorite.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactFavorite(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.favorite(contactId = contactId).toAppFunctionChangedCount() }

    /**
     * Removes the favorite mark from a contact.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactUnfavorite(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.unfavorite(contactId = contactId).toAppFunctionChangedCount() }

    /**
     * Deletes a contact. Use contactRestore with the same id to undo.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactDelete(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.delete(contactId = contactId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted contact.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactRestore(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.restore(contactId = contactId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a contact. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param contactId Id of the contact.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactTagAdd(
        contactId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.CONTACT, entityId = contactId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a contact. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param contactId Id of the contact.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactTagRemove(
        contactId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.CONTACT, entityId = contactId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Checklist

    /**
     * Lists checklists that are not deleted, by title, with how many of their items are checked.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of checklists.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistList(cursor: String? = null): AppFunctionChecklistSummaryPage = appFunctionCall { checklistAppAction.list(cursor = cursor).toAppFunctionChecklistSummaryPage() }

    /**
     * Gets one checklist with all its items that are not deleted and its linked tags. Returns null when the checklist does not exist or is deleted.
     *
     * @param checklistId Id of the checklist.
     * @param itemSort Order of the checklist items. added keeps the order the items were added; unchecked_first puts unchecked items first, each group in added order. Omit it to use the initial order (added).
     * @return The checklist with its items and tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistGet(
        checklistId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionChecklistItemSortValue.ADDED, AppActionChecklistItemSortValue.UNCHECKED_FIRST])
        itemSort: String? = null,
    ): AppFunctionChecklistDetail? = appFunctionCall { checklistAppAction.get(checklistId = checklistId, itemSort = itemSort)?.toAppFunctionChecklistDetail() }

    /**
     * Adds a checklist with optional unchecked items. The title must not be blank. Blank item contents are skipped.
     *
     * @param title Title of the checklist.
     * @param description Description of the checklist.
     * @param items Contents of the items, in the order to add them.
     * @param tagIds Ids of tags to link.
     * @return Id of the added checklist.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistAdd(
        title: String,
        description: String? = null,
        items: List<String> = emptyList(),
        tagIds: List<String> = emptyList(),
    ): AppFunctionId = appFunctionCall { checklistAppAction.add(title = title, description = description, items = items, tagIds = tagIds).toAppFunctionId() }

    /**
     * Updates the title or description of a checklist. Omitted fields keep their current value. A blank title keeps the current title. Items are changed with the checklist item functions. Tags are changed with checklistTagAdd and checklistTagRemove.
     *
     * @param checklistId Id of the checklist.
     * @param title New title.
     * @param description New description.
     * @return How many checklists were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistUpdate(
        checklistId: String,
        title: String? = null,
        description: String? = null,
    ): AppFunctionChangedCount = appFunctionCall { checklistAppAction.update(checklistId = checklistId, title = title, description = description).toAppFunctionChangedCount() }

    /**
     * Unchecks all checked items of a checklist at once. Returns how many items were unchecked.
     *
     * @param checklistId Id of the checklist.
     * @return How many items were unchecked.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistReset(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.reset(checklistId = checklistId).toAppFunctionChangedCount() }

    /**
     * Deletes a checklist. Use checklistRestore with the same id to undo.
     *
     * @param checklistId Id of the checklist.
     * @return How many checklists were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistDelete(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.delete(checklistId = checklistId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted checklist.
     *
     * @param checklistId Id of the checklist.
     * @return How many checklists were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistRestore(checklistId: String): AppFunctionChangedCount = appFunctionCall { checklistAppAction.restore(checklistId = checklistId).toAppFunctionChangedCount() }

    /**
     * Adds an unchecked item to the end of a checklist. The content must not be blank. The same content can be added more than once.
     *
     * @param checklistId Id of the checklist.
     * @param content Content of the item, one line.
     * @return How many items were added.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemAdd(
        checklistId: String,
        content: String,
    ): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.add(checklistId = checklistId, content = content).toAppFunctionChangedCount() }

    /**
     * Checks a checklist item.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemCheck(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.check(itemId = itemId).toAppFunctionChangedCount() }

    /**
     * Unchecks a checklist item.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemUncheck(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.uncheck(itemId = itemId).toAppFunctionChangedCount() }

    /**
     * Removes an item from its checklist. This cannot be undone; add the content again with checklistItemAdd if needed.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were removed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemDelete(itemId: String): AppFunctionChangedCount = appFunctionCall { checklistItemAppAction.delete(itemId = itemId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a checklist. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param checklistId Id of the checklist.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistTagAdd(
        checklistId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a checklist. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param checklistId Id of the checklist.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistTagRemove(
        checklistId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.CHECKLIST, entityId = checklistId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Qr

    /**
     * Lists saved QR codes that are not deleted, ordered by title.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of QR codes.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrList(cursor: String? = null): AppFunctionQrPage = appFunctionCall { qrAppAction.list(cursor = cursor).toAppFunctionQrPage() }

    /**
     * Gets one saved QR code with its linked tags. Returns null when the QR does not exist or is deleted.
     *
     * @param qrId Id of the QR.
     * @return The QR code with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrGet(qrId: String): AppFunctionQrDetail? = appFunctionCall { qrAppAction.get(qrId = qrId)?.toAppFunctionQrDetail() }

    /**
     * Adds a QR code. The title must not be blank and the value must not be empty. The value is the exact text encoded in the QR, such as a URL, plain text, a WIFI: string or a vCard.
     *
     * @param title Title of the QR.
     * @param description Description of the QR.
     * @param value Text encoded in the QR.
     * @param tagIds Ids of tags to link.
     * @return Id of the added QR code.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrAdd(
        title: String,
        description: String? = null,
        value: String,
        tagIds: List<String> = emptyList(),
    ): AppFunctionId = appFunctionCall { qrAppAction.add(title = title, description = description, value = value, tagIds = tagIds).toAppFunctionId() }

    /**
     * Updates the title, description or value of a QR code. Omitted fields keep their current value. A blank title keeps the current title. The value must not be empty. Tags are changed with qrTagAdd and qrTagRemove.
     *
     * @param qrId Id of the QR.
     * @param title New title.
     * @param description New description.
     * @param value New text encoded in the QR.
     * @return How many QR codes were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrUpdate(
        qrId: String,
        title: String? = null,
        description: String? = null,
        value: String? = null,
    ): AppFunctionChangedCount = appFunctionCall { qrAppAction.update(qrId = qrId, title = title, description = description, value = value).toAppFunctionChangedCount() }

    /**
     * Deletes a QR code. Use qrRestore with the same id to undo.
     *
     * @param qrId Id of the QR.
     * @return How many QR codes were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrDelete(qrId: String): AppFunctionChangedCount = appFunctionCall { qrAppAction.delete(qrId = qrId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted QR code.
     *
     * @param qrId Id of the QR.
     * @return How many QR codes were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrRestore(qrId: String): AppFunctionChangedCount = appFunctionCall { qrAppAction.restore(qrId = qrId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a QR code. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param qrId Id of the QR.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrTagAdd(
        qrId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.QR, entityId = qrId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a QR code. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param qrId Id of the QR.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrTagRemove(
        qrId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.QR, entityId = qrId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Music

    /**
     * Lists the music in the playlist that is not deleted.
     *
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of music.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicList(
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMusicPage = appFunctionCall { musicAppAction.list(sort = sort, cursor = cursor).toAppFunctionMusicPage() }

    /**
     * Gets one music in the playlist with its linked tags. Returns null when the music does not exist or is deleted.
     *
     * @param musicId Id of the music.
     * @return The music with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicGet(musicId: String): AppFunctionMusicDetail? = appFunctionCall { musicAppAction.get(musicId = musicId)?.toAppFunctionMusicDetail() }

    /**
     * Adds a music to the playlist. The title must not be blank. Video information is not fetched from the link, so give the title and artist yourself.
     *
     * @param title Title of the music.
     * @param artist Artist of the music.
     * @param link A YouTube video link. An empty string means no link. A non-empty link that is not a YouTube link is rejected.
     * @param tagIds Ids of tags to link.
     * @return Id of the added music.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicAdd(
        title: String,
        artist: String? = null,
        link: String? = null,
        tagIds: List<String> = emptyList(),
    ): AppFunctionId = appFunctionCall { musicAppAction.add(title = title, artist = artist, link = link, tagIds = tagIds).toAppFunctionId() }

    /**
     * Updates the title, artist or link of a music. Omitted fields keep their current value. A blank title keeps the current title. Tags are changed with musicTagAdd and musicTagRemove.
     *
     * @param musicId Id of the music.
     * @param title New title.
     * @param artist New artist.
     * @param link A YouTube video link. An empty string means no link. A non-empty link that is not a YouTube link is rejected.
     * @return How many music were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicUpdate(
        musicId: String,
        title: String? = null,
        artist: String? = null,
        link: String? = null,
    ): AppFunctionChangedCount = appFunctionCall { musicAppAction.update(musicId = musicId, title = title, artist = artist, link = link).toAppFunctionChangedCount() }

    /**
     * Deletes a music from the playlist. Use musicRestore with the same id to undo.
     *
     * @param musicId Id of the music.
     * @return How many music were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicDelete(musicId: String): AppFunctionChangedCount = appFunctionCall { musicAppAction.delete(musicId = musicId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted music.
     *
     * @param musicId Id of the music.
     * @return How many music were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicRestore(musicId: String): AppFunctionChangedCount = appFunctionCall { musicAppAction.restore(musicId = musicId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a music. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param musicId Id of the music.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicTagAdd(
        musicId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.MUSIC, entityId = musicId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a music. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param musicId Id of the music.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicTagRemove(
        musicId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.MUSIC, entityId = musicId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region DDay

    /**
     * Lists the D-Days shown on the D-Day home list by title, with the days left or passed counted from today. The filters the user chose in the app are applied.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of D-Days.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayList(cursor: String? = null): AppFunctionDDayPage = appFunctionCall { ddayAppAction.list(cursor = cursor).toAppFunctionDDayPage() }

    /**
     * Gets one D-Day with its linked tags and the days left or passed counted from today. Returns null when the D-Day does not exist or is deleted.
     *
     * @param ddayId Id of the D-Day.
     * @return The D-Day with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayGet(ddayId: String): AppFunctionDDayDetail? = appFunctionCall { ddayAppAction.get(ddayId = ddayId)?.toAppFunctionDDayDetail() }

    /**
     * Adds a D-Day. The title must not be blank and the end date must not be before the base date. Omitted values use what the D-Day add screen fills in first: today as the base date, counting the base date as day 1, no repeat intervals, no end date, clamp_to_month_end and a random color.
     *
     * @param title Title of the D-Day.
     * @param description Description of the D-Day.
     * @param color Color of the D-Day. A random color is used when omitted. Use a hex color like #4285F4.
     * @param baseDate Base date of the D-Day, like 2026-01-31. Today is used when omitted.
     * @param countsBaseDay Whether the base date counts as day 1. True when omitted.
     * @param repeats Intervals of anniversaries after the base date. Each interval is an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D (100 days), P3M (3 months) or P1Y (1 year). Weeks and times are not accepted.
     * @param endDate Last date anniversaries are shown, like 2026-12-31.
     * @param overflowPolicy What to do when a monthly anniversary falls on a date the month does not have. clamp_to_month_end uses the last day of the month, skip does not show it, next_month_first_day uses the 1st of the next month.
     * @param tagIds Ids of tags to link.
     * @return Id of the added D-Day.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayAdd(
        title: String,
        description: String? = null,
        color: String? = null,
        baseDate: String? = null,
        countsBaseDay: Boolean? = null,
        repeats: List<String> = emptyList(),
        endDate: String? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END, AppActionDDayOverflowPolicyValue.SKIP, AppActionDDayOverflowPolicyValue.NEXT_MONTH_FIRST_DAY])
        overflowPolicy: String? = null,
        tagIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            ddayAppAction
                .add(
                    title = title,
                    description = description,
                    color = color,
                    baseDate = baseDate,
                    countsBaseDay = countsBaseDay,
                    repeats = repeats,
                    endDate = endDate,
                    overflowPolicy = overflowPolicy,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    /**
     * Updates a D-Day. Omitted fields keep their current value. A blank title keeps the current title. The end date must not be before the base date after the update. Tags are changed with ddayTagAdd and ddayTagRemove.
     *
     * @param ddayId Id of the D-Day.
     * @param title New title.
     * @param description New description.
     * @param color New color. Use a hex color like #4285F4.
     * @param baseDate New base date, like 2026-01-31.
     * @param countsBaseDay Whether the base date counts as day 1.
     * @param repeats New repeat intervals. They replace all current intervals; pass an empty list to remove them. Each interval is an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D (100 days), P3M (3 months) or P1Y (1 year). Weeks and times are not accepted.
     * @param endDate New end date, like 2026-12-31.
     * @param clearEndDate Set true to remove the end date. It wins over endDate.
     * @param overflowPolicy What to do when a monthly anniversary falls on a date the month does not have. clamp_to_month_end uses the last day of the month, skip does not show it, next_month_first_day uses the 1st of the next month.
     * @return How many D-Days were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayUpdate(
        ddayId: String,
        title: String? = null,
        description: String? = null,
        color: String? = null,
        baseDate: String? = null,
        countsBaseDay: Boolean? = null,
        repeats: List<String>? = null,
        endDate: String? = null,
        clearEndDate: Boolean? = null,
        @AppFunctionStringValueConstraint(enumValues = [AppActionDDayOverflowPolicyValue.CLAMP_TO_MONTH_END, AppActionDDayOverflowPolicyValue.SKIP, AppActionDDayOverflowPolicyValue.NEXT_MONTH_FIRST_DAY])
        overflowPolicy: String? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            ddayAppAction
                .update(
                    ddayId = ddayId,
                    title = title,
                    description = description,
                    color = color,
                    baseDate = baseDate,
                    countsBaseDay = countsBaseDay,
                    repeats = repeats,
                    endDate = endDate,
                    clearEndDate = clearEndDate,
                    overflowPolicy = overflowPolicy,
                ).toAppFunctionChangedCount()
        }

    /**
     * Deletes a D-Day. Use ddayRestore with the same id to undo.
     *
     * @param ddayId Id of the D-Day.
     * @return How many D-Days were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayDelete(ddayId: String): AppFunctionChangedCount = appFunctionCall { ddayAppAction.delete(ddayId = ddayId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted D-Day.
     *
     * @param ddayId Id of the D-Day.
     * @return How many D-Days were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayRestore(ddayId: String): AppFunctionChangedCount = appFunctionCall { ddayAppAction.restore(ddayId = ddayId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a D-Day. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param ddayId Id of the D-Day.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayTagAdd(
        ddayId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.DDAY, entityId = ddayId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a D-Day. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param ddayId Id of the D-Day.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun ddayTagRemove(
        ddayId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.DDAY, entityId = ddayId, tagId = tagId).toAppFunctionDone() }

    // endregion

    // region Routine

    /**
     * Lists the routines shown on the routine home list, which are not finished, in progress first, then not started, by title within each, with this cycle's total and achievement rate counted from today.
     * The tag filter the user chose in the app is applied. The view scope filter is not, so routines not started and routines not done on today's day of the week are also listed.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of routines.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineList(cursor: String? = null): AppFunctionRoutinePage = appFunctionCall { routineListAppAction.list(cursor = cursor).toAppFunctionRoutinePage() }

    /**
     * Lists finished routines that are not deleted, in progress first, then not started, by title within each. The filters the user chose in the app are not applied.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of routines.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineFinishedList(cursor: String? = null): AppFunctionRoutinePage = appFunctionCall { routineListAppAction.finishedList(cursor = cursor).toAppFunctionRoutinePage() }

    /**
     * Gets one routine with its linked tags and this cycle's total and achievement rate counted from today. Returns null when the routine does not exist or is deleted.
     *
     * @param routineId Id of the routine.
     * @return The routine with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineGet(routineId: String): AppFunctionRoutineDetail? = appFunctionCall { routineAppAction.get(routineId = routineId)?.toAppFunctionRoutineDetail() }

    /**
     * Adds a routine with a random color. The title must not be blank and amounts must be between 1 and 9999999. The routine is added as not finished. Omitted values use what the routine add screen fills in first: today, FREQ=DAILY, no unit, no goal and 1 per record.
     *
     * @param title Title of the routine.
     * @param description Description of the routine.
     * @param startDate First date records can be added, like 2026-01-31. Today is used when omitted.
     * @param cycle How often the routine repeats. FREQ=DAILY when omitted. FREQ=DAILY is every day, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR is every day done on weekdays only,
     * FREQ=DAILY;BYDAY=SA,SU is every day done on weekends only, FREQ=WEEKLY;WKST=SU is every week from Sunday to Saturday, FREQ=MONTHLY is every month from the 1st to the last day.
     * @param unit Unit of the amounts. The routine has no unit when omitted. Free text of at most 10 characters without line breaks, such as times, ml or pages. Spaces around it are removed.
     * @param goalAmount Amount to reach in each cycle, between 1 and 9999999. The routine has no goal when omitted.
     * @param amountPerRecord Amount of one record when no amount is given, between 1 and 9999999. 1 when omitted.
     * @param tagIds Ids of tags to link.
     * @return Id of the added routine.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineAdd(
        title: String,
        description: String? = null,
        startDate: String? = null,
        @AppFunctionStringValueConstraint(
            enumValues = [
                AppActionRoutineCycleValue.DAILY,
                AppActionRoutineCycleValue.WEEKDAYS,
                AppActionRoutineCycleValue.WEEKENDS,
                AppActionRoutineCycleValue.WEEKLY,
                AppActionRoutineCycleValue.MONTHLY,
            ],
        )
        cycle: String? = null,
        unit: String? = null,
        goalAmount: Int? = null,
        amountPerRecord: Int? = null,
        tagIds: List<String> = emptyList(),
    ): AppFunctionId =
        appFunctionCall {
            routineAppAction
                .add(
                    title = title,
                    description = description,
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    amountPerRecord = amountPerRecord,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    /**
     * Updates a routine. Omitted fields keep their current value. A blank title keeps the current title. After the update amounts must be between 1 and 9999999. Tags are changed with routineTagAdd and routineTagRemove, and whether the routine is finished with routineFinish and routineRestart.
     *
     * @param routineId Id of the routine.
     * @param title New title.
     * @param description New description.
     * @param color New color. Use a hex color like #4285F4.
     * @param startDate New start date, like 2026-01-31.
     * @param cycle New cycle. FREQ=DAILY is every day, FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR is every day done on weekdays only, FREQ=DAILY;BYDAY=SA,SU is every day done on weekends only, FREQ=WEEKLY;WKST=SU is every week from Sunday to Saturday, FREQ=MONTHLY is every month from the 1st to the last day.
     * @param unit New unit. Amounts already recorded are kept and read in the new unit. An empty string removes the unit. Free text of at most 10 characters without line breaks, such as times, ml or pages. Spaces around it are removed.
     * @param goalAmount New goal amount, between 1 and 9999999.
     * @param clearGoalAmount Set true to remove the goal amount. It wins over goalAmount.
     * @param amountPerRecord New amount of one record when no amount is given, between 1 and 9999999.
     * @return How many routines were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineUpdate(
        routineId: String,
        title: String? = null,
        description: String? = null,
        color: String? = null,
        startDate: String? = null,
        @AppFunctionStringValueConstraint(
            enumValues = [
                AppActionRoutineCycleValue.DAILY,
                AppActionRoutineCycleValue.WEEKDAYS,
                AppActionRoutineCycleValue.WEEKENDS,
                AppActionRoutineCycleValue.WEEKLY,
                AppActionRoutineCycleValue.MONTHLY,
            ],
        )
        cycle: String? = null,
        unit: String? = null,
        goalAmount: Int? = null,
        clearGoalAmount: Boolean? = null,
        amountPerRecord: Int? = null,
    ): AppFunctionChangedCount =
        appFunctionCall {
            routineAppAction
                .update(
                    routineId = routineId,
                    title = title,
                    description = description,
                    color = color,
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    clearGoalAmount = clearGoalAmount,
                    amountPerRecord = amountPerRecord,
                ).toAppFunctionChangedCount()
        }

    /**
     * Marks a routine as finished. Its records are kept and records can still be added and removed.
     *
     * @param routineId Id of the routine.
     * @return How many routines were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineFinish(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.finish(routineId = routineId).toAppFunctionChangedCount() }

    /**
     * Marks a finished routine as not finished.
     *
     * @param routineId Id of the routine.
     * @return How many routines were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineRestart(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.restart(routineId = routineId).toAppFunctionChangedCount() }

    /**
     * Deletes a routine. Its records are kept. Use routineRestore with the same id to bring it back with its records.
     *
     * @param routineId Id of the routine.
     * @return How many routines were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineDelete(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.delete(routineId = routineId).toAppFunctionChangedCount() }

    /**
     * Restores a deleted routine with its records.
     *
     * @param routineId Id of the routine.
     * @return How many routines were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineRestore(routineId: String): AppFunctionChangedCount = appFunctionCall { routineAppAction.restore(routineId = routineId).toAppFunctionChangedCount() }

    /**
     * Links a tag to a routine. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param routineId Id of the routine.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineTagAdd(
        routineId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.add(kind = AppActionEntityTagKind.ROUTINE, entityId = routineId, tagId = tagId).toAppFunctionDone() }

    /**
     * Unlinks a tag from a routine. Returns done false and changes nothing when either item does not exist or is deleted.
     *
     * @param routineId Id of the routine.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineTagRemove(
        routineId: String,
        tagId: String,
    ): AppFunctionDone = appFunctionCall { entityTagAppAction.remove(kind = AppActionEntityTagKind.ROUTINE, entityId = routineId, tagId = tagId).toAppFunctionDone() }

    /**
     * Lists the records of a routine that are not removed, latest date first and, within a date, latest added first. Amounts are in the unit of the routine. Returns no records when the routine does not exist or is deleted.
     *
     * @param routineId Id of the routine.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of routine records.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineRecordList(
        routineId: String,
        cursor: String? = null,
    ): AppFunctionRoutineRecordPage = appFunctionCall { routineRecordAppAction.list(routineId = routineId, cursor = cursor).toAppFunctionRoutineRecordPage() }

    /**
     * Adds a record to a routine. The date must be on or after the start date of the routine and the amount must be between 1 and 9999999. Returns changedCount 0 and adds nothing when the routine does not exist or is deleted.
     *
     * @param routineId Id of the routine.
     * @param date Date of the record, like 2026-01-31. Today is used when omitted.
     * @param amount Amount of the record in the unit of the routine. The routine's amount per record is used when omitted.
     * @return How many records were added.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineRecordAdd(
        routineId: String,
        date: String? = null,
        amount: Int? = null,
    ): AppFunctionChangedCount = appFunctionCall { routineRecordAppAction.add(routineId = routineId, date = date, amount = amount).toAppFunctionChangedCount() }

    /**
     * Removes a record from its routine. This cannot be undone; add it again with routineRecordAdd with the same date and amount if needed.
     *
     * @param recordId Id of the routine record.
     * @return How many records were removed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun routineRecordRemove(recordId: String): AppFunctionChangedCount = appFunctionCall { routineRecordAppAction.remove(recordId = recordId).toAppFunctionChangedCount() }

    // endregion

    // region Search

    /**
     * Searches memos that are not deleted, including finished ones, by the query, like the memo results of the search screen.
     *
     * @param query Search query. A blank query returns no results.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found memos.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchMemo(
        query: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionMemoPage = appFunctionCall { searchAppAction.memo(query = query, sort = sort, cursor = cursor).toAppFunctionMemoPage() }

    /**
     * Searches tags by the query, like the tag results of the search screen.
     *
     * @param query Search query. A blank query returns no results.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found tags.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchTag(
        query: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionSearchTagPage = appFunctionCall { searchAppAction.tag(query = query, sort = sort, cursor = cursor).toAppFunctionSearchTagPage() }

    /**
     * Searches saved places by the query, like the place results of the search screen. It does not search places outside the app.
     *
     * @param query Search query. A blank query returns no results.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found places.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchPlace(
        query: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionSearchPlacePage = appFunctionCall { searchAppAction.place(query = query, sort = sort, cursor = cursor).toAppFunctionSearchPlacePage() }

    /**
     * Searches saved webs by the query, like the web results of the search screen.
     *
     * @param query Search query. A blank query returns no results.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found webs.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchWeb(
        query: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionSearchWebPage = appFunctionCall { searchAppAction.web(query = query, sort = sort, cursor = cursor).toAppFunctionSearchWebPage() }

    /**
     * Searches saved D-Days by the query, like the D-Day results of the search screen, with the days left or passed counted from today.
     *
     * @param query Search query. A blank query returns no results.
     * @param sort Sort order. Omit it to use the list's initial order (title).
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found D-Days.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchDday(
        query: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionSortValue.TITLE, AppActionSortValue.RECENTLY_UPDATED])
        sort: String? = null,
        cursor: String? = null,
    ): AppFunctionSearchDDayPage = appFunctionCall { searchAppAction.dday(query = query, sort = sort, cursor = cursor).toAppFunctionSearchDDayPage() }

    /**
     * Searches saved routines by the query, like the routine results of the search screen, finished routines included, in progress first, then not started, by title within each, with this cycle's total and achievement rate counted from today.
     *
     * @param query Search query. A blank query returns no results.
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of found routines.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun searchRoutine(
        query: String,
        cursor: String? = null,
    ): AppFunctionSearchRoutinePage = appFunctionCall { searchAppAction.routine(query = query, cursor = cursor).toAppFunctionSearchRoutinePage() }

    // endregion

    // region Calendar

    /**
     * Lists the memos shown on the calendar whose period overlaps the date range. Finished memos are included. The calendar tag filter the user chose in the app is applied.
     *
     * @param startDate First date of the range, like 2026-01-01.
     * @param endDate Last date of the range, inclusive, like 2026-01-31.
     * @return Memos shown on the calendar.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun calendarMemoList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionCalendarMemo> = appFunctionCall { calendarAppAction.memoList(startDate = startDate, endDate = endDate).map { memo -> memo.toAppFunctionCalendarMemo() } }

    /**
     * Lists contact birthdays shown on the calendar within the date range, as solar dates. Lunar birthdays appear only for years whose lunar data is saved on this device.
     *
     * @param startDate First date of the range, like 2026-01-01.
     * @param endDate Last date of the range, inclusive, like 2026-01-31.
     * @return Contact birthdays shown on the calendar.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun calendarBirthdayList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionCalendarBirthday> = appFunctionCall { calendarAppAction.birthdayList(startDate = startDate, endDate = endDate).map { birthday -> birthday.toAppFunctionCalendarBirthday() } }

    /**
     * Lists the base dates and anniversaries of D-Days shown on the calendar within the date range, by date. Each label tells whether it is the base day or which count of days, months or years it is. The calendar tag filter the user chose in the app is applied.
     *
     * @param startDate First date of the range, like 2026-01-01.
     * @param endDate Last date of the range, inclusive, like 2026-01-31.
     * @return D-Days shown on the calendar.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun calendarDdayList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionCalendarDDay> = appFunctionCall { calendarAppAction.ddayList(startDate = startDate, endDate = endDate).map { dday -> dday.toAppFunctionCalendarDDay() } }

    /**
     * Lists the holidays shown on the calendar for a year, for the holiday countries the user chose and without the holidays the user hid. isHoliday is false for named days that are not days off. Only years already saved on this device are returned.
     *
     * @param year Year like 2026.
     * @return Holidays shown on the calendar.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun holidayList(year: Int): List<AppFunctionHoliday> = appFunctionCall { calendarAppAction.holidayList(year = year).map { holiday -> holiday.toAppFunctionHoliday() } }

    // endregion

    // region Lunar

    /**
     * Lists the lunar dates of solar dates in a range, from the lunar data saved on this device. Dates whose lunar data has not been downloaded yet are missing from the result.
     *
     * @param startDate First date of the range, like 2026-01-01.
     * @param endDate Last date of the range, inclusive, like 2026-01-31.
     * @return Lunar dates of the solar dates.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun lunarList(
        startDate: String,
        endDate: String,
    ): List<AppFunctionLunarDate> = appFunctionCall { lunarAppAction.list(startDate = startDate, endDate = endDate).map { lunarDate -> lunarDate.toAppFunctionLunarDate() } }

    // endregion
}
