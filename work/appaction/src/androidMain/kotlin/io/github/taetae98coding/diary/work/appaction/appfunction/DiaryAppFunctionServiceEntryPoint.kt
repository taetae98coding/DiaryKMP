package io.github.taetae98coding.diary.work.appaction.appfunction

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.AppFunctionStringValueConstraint
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.AppFunctionCalendarBirthday
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.AppFunctionCalendarMemo
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.AppFunctionHoliday
import io.github.taetae98coding.diary.work.appaction.appfunction.calendar.CalendarAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.AppFunctionChecklistSummaryPage
import io.github.taetae98coding.diary.work.appaction.appfunction.checklist.ChecklistAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.AppFunctionContactPage
import io.github.taetae98coding.diary.work.appaction.appfunction.contact.ContactAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionDone
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.lunar.AppFunctionLunarDate
import io.github.taetae98coding.diary.work.appaction.appfunction.lunar.LunarAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDateTimeInput
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.AppFunctionMemoPage
import io.github.taetae98coding.diary.work.appaction.appfunction.memo.MemoAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.memolink.MemoLinkAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusic
import io.github.taetae98coding.diary.work.appaction.appfunction.music.AppFunctionMusicPage
import io.github.taetae98coding.diary.work.appaction.appfunction.music.MusicAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlaceDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlaceList
import io.github.taetae98coding.diary.work.appaction.appfunction.place.AppFunctionPlacePage
import io.github.taetae98coding.diary.work.appaction.appfunction.place.PlaceAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQr
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.AppFunctionQrPage
import io.github.taetae98coding.diary.work.appaction.appfunction.qr.QrAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.search.AppFunctionSearchPlacePage
import io.github.taetae98coding.diary.work.appaction.appfunction.search.AppFunctionSearchTagPage
import io.github.taetae98coding.diary.work.appaction.appfunction.search.AppFunctionSearchWebPage
import io.github.taetae98coding.diary.work.appaction.appfunction.search.SearchAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagPage
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagPlacePage
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.AppFunctionTagWebPage
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.TagAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.TagItemAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.tag.TagLinkAppFunction
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebDetail
import io.github.taetae98coding.diary.work.appaction.appfunction.web.AppFunctionWebPage
import io.github.taetae98coding.diary.work.appaction.appfunction.web.WebAppFunction
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItemSortValue
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactBirthdayCalendarValue
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.core.AppActionSortValue
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTagScopeValue
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// AppFunction 컴파일러는 이 클래스에 직접 선언한 함수만 읽고, Android 16은 앱마다 서비스 하나만 색인하므로 모든 도메인의 함수를 여기에 둔다.
// 함수 본문은 도메인별 XxxAppFunction에 넘기기만 하고, 도메인마다 region 하나를 쓴다.
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
    private val memoAppFunction: MemoAppFunction by lazy { MemoAppFunction(memoAppAction = get()) }
    private val calendarAppFunction: CalendarAppFunction by lazy { CalendarAppFunction(calendarAppAction = get()) }
    private val checklistAppFunction: ChecklistAppFunction by lazy { ChecklistAppFunction(checklistAppAction = get()) }
    private val contactAppFunction: ContactAppFunction by lazy { ContactAppFunction(contactAppAction = get()) }
    private val lunarAppFunction: LunarAppFunction by lazy { LunarAppFunction(lunarAppAction = get()) }
    private val memoLinkAppFunction: MemoLinkAppFunction by lazy { MemoLinkAppFunction(memoLinkAppAction = get()) }
    private val musicAppFunction: MusicAppFunction by lazy { MusicAppFunction(musicAppAction = get()) }
    private val placeAppFunction: PlaceAppFunction by lazy { PlaceAppFunction(placeAppAction = get()) }
    private val qrAppFunction: QrAppFunction by lazy { QrAppFunction(qrAppAction = get()) }
    private val searchAppFunction: SearchAppFunction by lazy { SearchAppFunction(searchAppAction = get()) }
    private val tagAppFunction: TagAppFunction by lazy { TagAppFunction(tagAppAction = get()) }
    private val tagItemAppFunction: TagItemAppFunction by lazy { TagItemAppFunction(tagItemAppAction = get()) }
    private val tagLinkAppFunction: TagLinkAppFunction by lazy { TagLinkAppFunction(tagLinkAppAction = get()) }
    private val webAppFunction: WebAppFunction by lazy { WebAppFunction(webAppAction = get()) }

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
    ): AppFunctionMemoPage = memoAppFunction.list(sort = sort, cursor = cursor)

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
    ): AppFunctionMemoPage = memoAppFunction.finishedList(sort = sort, cursor = cursor)

    /**
     * Gets one memo with its linked tags, places, webs and contacts. Returns null when the memo does not exist.
     *
     * @param memoId Id of the memo.
     * @return The memo with its links, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoGet(memoId: String): AppFunctionMemoDetail? = memoAppFunction.get(memoId = memoId)

    /**
     * Adds a memo. The title must not be blank. Linked items must exist and not be deleted.
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
        memoAppFunction.add(
            title = title,
            description = description,
            color = color,
            dateTime = dateTime,
            primaryTagId = primaryTagId,
            tagIds = tagIds,
            placeIds = placeIds,
            webIds = webIds,
            contactIds = contactIds,
        )

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
        memoAppFunction.update(
            memoId = memoId,
            title = title,
            description = description,
            color = color,
            dateTime = dateTime,
            clearDateTime = clearDateTime,
        )

    /**
     * Copies a memo with its links into a new unfinished memo.
     *
     * @param memoId Id of the memo.
     * @return Id of the new memo.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoCopy(memoId: String): AppFunctionId = memoAppFunction.copy(memoId = memoId)

    /**
     * Marks a memo as finished.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoFinish(memoId: String): AppFunctionChangedCount = memoAppFunction.finish(memoId = memoId)

    /**
     * Marks a finished memo as not finished.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoRestart(memoId: String): AppFunctionChangedCount = memoAppFunction.restart(memoId = memoId)

    /**
     * Deletes a memo. Use memoRestore with the same id to undo.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoDelete(memoId: String): AppFunctionChangedCount = memoAppFunction.delete(memoId = memoId)

    /**
     * Restores a deleted memo.
     *
     * @param memoId Id of the memo.
     * @return How many memos were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoRestore(memoId: String): AppFunctionChangedCount = memoAppFunction.restore(memoId = memoId)

    // endregion

    // region MemoLink

    /**
     * Links a tag to a memo.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoTagAdd(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = memoLinkAppFunction.tagAdd(memoId = memoId, tagId = tagId)

    /**
     * Unlinks a tag from a memo. If it is the primary tag, the primary tag is cleared too.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoTagRemove(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = memoLinkAppFunction.tagRemove(memoId = memoId, tagId = tagId)

    /**
     * Sets the primary tag of a memo. The tag is linked too when it is not linked yet.
     *
     * @param memoId Id of the memo.
     * @param tagId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPrimaryTagSet(
        memoId: String,
        tagId: String,
    ): AppFunctionDone = memoLinkAppFunction.primaryTagSet(memoId = memoId, tagId = tagId)

    /**
     * Clears the primary tag of a memo. The tag stays linked.
     *
     * @param memoId Id of the memo.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPrimaryTagUnset(memoId: String): AppFunctionDone = memoLinkAppFunction.primaryTagUnset(memoId = memoId)

    /**
     * Links a place to a memo.
     *
     * @param memoId Id of the memo.
     * @param placeId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPlaceAdd(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = memoLinkAppFunction.placeAdd(memoId = memoId, placeId = placeId)

    /**
     * Unlinks a place from a memo.
     *
     * @param memoId Id of the memo.
     * @param placeId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoPlaceRemove(
        memoId: String,
        placeId: String,
    ): AppFunctionDone = memoLinkAppFunction.placeRemove(memoId = memoId, placeId = placeId)

    /**
     * Links a web to a memo.
     *
     * @param memoId Id of the memo.
     * @param webId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoWebAdd(
        memoId: String,
        webId: String,
    ): AppFunctionDone = memoLinkAppFunction.webAdd(memoId = memoId, webId = webId)

    /**
     * Unlinks a web from a memo.
     *
     * @param memoId Id of the memo.
     * @param webId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoWebRemove(
        memoId: String,
        webId: String,
    ): AppFunctionDone = memoLinkAppFunction.webRemove(memoId = memoId, webId = webId)

    /**
     * Links a contact to a memo.
     *
     * @param memoId Id of the memo.
     * @param contactId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoContactAdd(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = memoLinkAppFunction.contactAdd(memoId = memoId, contactId = contactId)

    /**
     * Unlinks a contact from a memo.
     *
     * @param memoId Id of the memo.
     * @param contactId Id of the linked item.
     * @return Whether the request was done.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun memoContactRemove(
        memoId: String,
        contactId: String,
    ): AppFunctionDone = memoLinkAppFunction.contactRemove(memoId = memoId, contactId = contactId)

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
    ): AppFunctionTagPage = tagAppFunction.list(sort = sort, cursor = cursor)

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
    ): AppFunctionTagPage = tagAppFunction.finishedList(sort = sort, cursor = cursor)

    /**
     * Gets one tag with the tags it links to (its child tags). Returns null when the tag does not exist. Use tagMemoList, tagWebList and tagPlaceList for the items linked to the tag.
     *
     * @param tagId Id of the tag.
     * @return The tag with its child tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagGet(tagId: String): AppFunctionTagDetail? = tagAppFunction.get(tagId = tagId)

    /**
     * Adds a tag. The title must not be blank. Linked tags must exist and not be deleted.
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
    ): AppFunctionId = tagAppFunction.add(title = title, emoji = emoji, description = description, color = color, linkedTagIds = linkedTagIds)

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
    ): AppFunctionChangedCount = tagAppFunction.update(tagId = tagId, emoji = emoji, title = title, description = description, color = color)

    /**
     * Marks a tag as finished.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagFinish(tagId: String): AppFunctionChangedCount = tagAppFunction.finish(tagId = tagId)

    /**
     * Marks a finished tag as not finished.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRestart(tagId: String): AppFunctionChangedCount = tagAppFunction.restart(tagId = tagId)

    /**
     * Deletes a tag. Use tagRestore with the same id to undo.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagDelete(tagId: String): AppFunctionChangedCount = tagAppFunction.delete(tagId = tagId)

    /**
     * Restores a deleted tag.
     *
     * @param tagId Id of the tag.
     * @return How many tags were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagRestore(tagId: String): AppFunctionChangedCount = tagAppFunction.restore(tagId = tagId)

    // endregion

    // region TagLink

    /**
     * Links a tag to another tag, making the linked tag a child of the tag. Links are one-way and a tag cannot link to itself. Linking does not change the tags of memos, webs or places.
     *
     * @param tagId Id of the tag.
     * @param linkedTagId Id of the tag that the tag links to (the child tag).
     * @return Whether the request was carried out.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagLinkAdd(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = tagLinkAppFunction.add(tagId = tagId, linkedTagId = linkedTagId)

    /**
     * Removes the link from a tag to another tag. The link in the opposite direction is kept.
     *
     * @param tagId Id of the tag.
     * @param linkedTagId Id of the tag that the tag links to (the child tag).
     * @return Whether the request was carried out.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun tagLinkRemove(
        tagId: String,
        linkedTagId: String,
    ): AppFunctionDone = tagLinkAppFunction.remove(tagId = tagId, linkedTagId = linkedTagId)

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
    ): AppFunctionMemoPage = tagItemAppFunction.memoList(tagId = tagId, scope = scope, sort = sort, cursor = cursor)

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
    ): AppFunctionMemoPage = tagItemAppFunction.memoFinishedList(tagId = tagId, sort = sort, cursor = cursor)

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
    ): AppFunctionTagWebPage = tagItemAppFunction.webList(tagId = tagId, scope = scope, sort = sort, cursor = cursor)

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
    ): AppFunctionTagPlacePage = tagItemAppFunction.placeList(tagId = tagId, scope = scope, sort = sort, cursor = cursor)

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
    ): AppFunctionPlacePage = placeAppFunction.list(sort = sort, cursor = cursor)

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
    ): AppFunctionPlaceList = placeAppFunction.listInArea(south = south, north = north, west = west, east = east, sort = sort)

    /**
     * Gets one place with its linked tags. Returns null when the place does not exist.
     *
     * @param placeId Id of the place.
     * @return The place with its tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeGet(placeId: String): AppFunctionPlaceDetail? = placeAppFunction.get(placeId = placeId)

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
    ): AppFunctionMemoPage = placeAppFunction.memoList(placeId = placeId, sort = sort, cursor = cursor)

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
    ): AppFunctionMemoPage = placeAppFunction.memoFinishedList(placeId = placeId, sort = sort, cursor = cursor)

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
        placeAppFunction.add(
            title = title,
            latitude = latitude,
            longitude = longitude,
            address = address,
            description = description,
            color = color,
            tagIds = tagIds,
        )

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
        placeAppFunction.update(
            placeId = placeId,
            title = title,
            description = description,
            color = color,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )

    /**
     * Deletes a place. Links to memos are kept. Use placeRestore with the same id to undo.
     *
     * @param placeId Id of the place.
     * @return How many places were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeDelete(placeId: String): AppFunctionChangedCount = placeAppFunction.delete(placeId = placeId)

    /**
     * Restores a deleted place.
     *
     * @param placeId Id of the place.
     * @return How many places were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeRestore(placeId: String): AppFunctionChangedCount = placeAppFunction.restore(placeId = placeId)

    /**
     * Links a tag to a place.
     *
     * @param placeId Id of the place.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeTagAdd(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = placeAppFunction.tagAdd(placeId = placeId, tagId = tagId)

    /**
     * Unlinks a tag from a place.
     *
     * @param placeId Id of the place.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun placeTagRemove(
        placeId: String,
        tagId: String,
    ): AppFunctionDone = placeAppFunction.tagRemove(placeId = placeId, tagId = tagId)

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
    ): AppFunctionWebPage = webAppFunction.list(sort = sort, cursor = cursor)

    /**
     * Gets one web with its request headers and linked tags. Returns null when the web does not exist.
     *
     * @param webId Id of the web.
     * @return The web with its request headers and tags, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webGet(webId: String): AppFunctionWebDetail? = webAppFunction.get(webId = webId)

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
    ): AppFunctionMemoPage = webAppFunction.memoList(webId = webId, sort = sort, cursor = cursor)

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
    ): AppFunctionMemoPage = webAppFunction.memoFinishedList(webId = webId, sort = sort, cursor = cursor)

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
    ): AppFunctionId = webAppFunction.add(title = title, url = url, description = description, headers = headers, tagIds = tagIds)

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
    ): AppFunctionChangedCount = webAppFunction.update(webId = webId, title = title, description = description, url = url, headers = headers)

    /**
     * Deletes a web. Links to memos are kept. Use webRestore with the same id to undo.
     *
     * @param webId Id of the web.
     * @return How many webs were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webDelete(webId: String): AppFunctionChangedCount = webAppFunction.delete(webId = webId)

    /**
     * Restores a deleted web.
     *
     * @param webId Id of the web.
     * @return How many webs were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webRestore(webId: String): AppFunctionChangedCount = webAppFunction.restore(webId = webId)

    /**
     * Links a tag to a web.
     *
     * @param webId Id of the web.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webTagAdd(
        webId: String,
        tagId: String,
    ): AppFunctionDone = webAppFunction.tagAdd(webId = webId, tagId = tagId)

    /**
     * Unlinks a tag from a web.
     *
     * @param webId Id of the web.
     * @param tagId Id of the tag.
     * @return Whether the request was completed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun webTagRemove(
        webId: String,
        tagId: String,
    ): AppFunctionDone = webAppFunction.tagRemove(webId = webId, tagId = tagId)

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
    ): AppFunctionContactPage = contactAppFunction.list(sort = sort, cursor = cursor)

    /**
     * Gets one contact with all its details. Returns null when the contact does not exist.
     *
     * @param contactId Id of the contact.
     * @return The contact with all its details, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactGet(contactId: String): AppFunctionContactDetail? = contactAppFunction.get(contactId = contactId)

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
    ): AppFunctionMemoPage = contactAppFunction.memoList(contactId = contactId, sort = sort, cursor = cursor)

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
    ): AppFunctionMemoPage = contactAppFunction.memoFinishedList(contactId = contactId, sort = sort, cursor = cursor)

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
     * @return Id of the added contact.
     */
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
    ): AppFunctionId =
        contactAppFunction.add(
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
        )

    /**
     * Updates the details of a contact. Omitted fields keep their current value. A blank name keeps the current name. Changing only the birthday date keeps its calendar.
     * Removing the birthday also removes the birth year unless a birth year is given. Favorite is changed with contactFavorite and contactUnfavorite.
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
        contactAppFunction.update(
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
        )

    /**
     * Marks a contact as a favorite.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactFavorite(contactId: String): AppFunctionChangedCount = contactAppFunction.favorite(contactId = contactId)

    /**
     * Removes the favorite mark from a contact.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactUnfavorite(contactId: String): AppFunctionChangedCount = contactAppFunction.unfavorite(contactId = contactId)

    /**
     * Deletes a contact. Use contactRestore with the same id to undo.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactDelete(contactId: String): AppFunctionChangedCount = contactAppFunction.delete(contactId = contactId)

    /**
     * Restores a deleted contact.
     *
     * @param contactId Id of the contact.
     * @return How many contacts were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun contactRestore(contactId: String): AppFunctionChangedCount = contactAppFunction.restore(contactId = contactId)

    // endregion

    // region Checklist

    /**
     * Lists checklists that are not deleted, by title, with how many of their items are checked.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of checklists.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistList(cursor: String? = null): AppFunctionChecklistSummaryPage = checklistAppFunction.list(cursor = cursor)

    /**
     * Gets one checklist with all its items that are not deleted. Returns null when the checklist does not exist.
     *
     * @param checklistId Id of the checklist.
     * @param itemSort Order of the checklist items. added keeps the order the items were added; unchecked_first puts unchecked items first, each group in added order. Omit it to use the initial order (added).
     * @return The checklist with its items, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistGet(
        checklistId: String,
        @AppFunctionStringValueConstraint(enumValues = [AppActionChecklistItemSortValue.ADDED, AppActionChecklistItemSortValue.UNCHECKED_FIRST])
        itemSort: String? = null,
    ): AppFunctionChecklistDetail? = checklistAppFunction.get(checklistId = checklistId, itemSort = itemSort)

    /**
     * Adds a checklist with optional unchecked items. The title must not be blank. Blank item contents are skipped.
     *
     * @param title Title of the checklist.
     * @param description Description of the checklist.
     * @param items Contents of the items, in the order to add them.
     * @return Id of the added checklist.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistAdd(
        title: String,
        description: String? = null,
        items: List<String> = emptyList(),
    ): AppFunctionId = checklistAppFunction.add(title = title, description = description, items = items)

    /**
     * Updates the title or description of a checklist. Omitted fields keep their current value. A blank title keeps the current title. Items are changed with the checklist item functions.
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
    ): AppFunctionChangedCount = checklistAppFunction.update(checklistId = checklistId, title = title, description = description)

    /**
     * Unchecks all checked items of a checklist at once. Returns how many items were unchecked.
     *
     * @param checklistId Id of the checklist.
     * @return How many items were unchecked.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistReset(checklistId: String): AppFunctionChangedCount = checklistAppFunction.reset(checklistId = checklistId)

    /**
     * Deletes a checklist. Use checklistRestore with the same id to undo.
     *
     * @param checklistId Id of the checklist.
     * @return How many checklists were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistDelete(checklistId: String): AppFunctionChangedCount = checklistAppFunction.delete(checklistId = checklistId)

    /**
     * Restores a deleted checklist.
     *
     * @param checklistId Id of the checklist.
     * @return How many checklists were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistRestore(checklistId: String): AppFunctionChangedCount = checklistAppFunction.restore(checklistId = checklistId)

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
    ): AppFunctionChangedCount = checklistAppFunction.itemAdd(checklistId = checklistId, content = content)

    /**
     * Checks a checklist item.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemCheck(itemId: String): AppFunctionChangedCount = checklistAppFunction.itemCheck(itemId = itemId)

    /**
     * Unchecks a checklist item.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemUncheck(itemId: String): AppFunctionChangedCount = checklistAppFunction.itemUncheck(itemId = itemId)

    /**
     * Removes an item from its checklist. This cannot be undone; add the content again with checklistItemAdd if needed.
     *
     * @param itemId Id of the checklist item.
     * @return How many items were removed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun checklistItemDelete(itemId: String): AppFunctionChangedCount = checklistAppFunction.itemDelete(itemId = itemId)

    // endregion

    // region Qr

    /**
     * Lists saved QR codes that are not deleted, ordered by title.
     *
     * @param cursor The nextCursor value from the previous page. Omit it to read the first page.
     * @return One page of QR codes.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrList(cursor: String? = null): AppFunctionQrPage = qrAppFunction.list(cursor = cursor)

    /**
     * Gets one saved QR code. Returns null when the QR does not exist.
     *
     * @param qrId Id of the QR.
     * @return The QR code, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrGet(qrId: String): AppFunctionQr? = qrAppFunction.get(qrId = qrId)

    /**
     * Adds a QR code. The title must not be blank and the value must not be empty. The value is the exact text encoded in the QR, such as a URL, plain text, a WIFI: string or a vCard.
     *
     * @param title Title of the QR.
     * @param description Description of the QR.
     * @param value Text encoded in the QR.
     * @return Id of the added QR code.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrAdd(
        title: String,
        description: String? = null,
        value: String,
    ): AppFunctionId = qrAppFunction.add(title = title, description = description, value = value)

    /**
     * Updates the title, description or value of a QR code. Omitted fields keep their current value. A blank title keeps the current title. The value must not be empty.
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
    ): AppFunctionChangedCount = qrAppFunction.update(qrId = qrId, title = title, description = description, value = value)

    /**
     * Deletes a QR code. Use qrRestore with the same id to undo.
     *
     * @param qrId Id of the QR.
     * @return How many QR codes were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrDelete(qrId: String): AppFunctionChangedCount = qrAppFunction.delete(qrId = qrId)

    /**
     * Restores a deleted QR code.
     *
     * @param qrId Id of the QR.
     * @return How many QR codes were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun qrRestore(qrId: String): AppFunctionChangedCount = qrAppFunction.restore(qrId = qrId)

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
    ): AppFunctionMusicPage = musicAppFunction.list(sort = sort, cursor = cursor)

    /**
     * Gets one music in the playlist. Returns null when the music does not exist.
     *
     * @param musicId Id of the music.
     * @return The music, or null.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicGet(musicId: String): AppFunctionMusic? = musicAppFunction.get(musicId = musicId)

    /**
     * Adds a music to the playlist. The title must not be blank. Video information is not fetched from the link, so give the title and artist yourself.
     *
     * @param title Title of the music.
     * @param artist Artist of the music.
     * @param link A YouTube video link. An empty string means no link. A non-empty link that is not a YouTube link is rejected.
     * @return Id of the added music.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicAdd(
        title: String,
        artist: String? = null,
        link: String? = null,
    ): AppFunctionId = musicAppFunction.add(title = title, artist = artist, link = link)

    /**
     * Updates the title, artist or link of a music. Omitted fields keep their current value. A blank title keeps the current title.
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
    ): AppFunctionChangedCount = musicAppFunction.update(musicId = musicId, title = title, artist = artist, link = link)

    /**
     * Deletes a music from the playlist. Use musicRestore with the same id to undo.
     *
     * @param musicId Id of the music.
     * @return How many music were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicDelete(musicId: String): AppFunctionChangedCount = musicAppFunction.delete(musicId = musicId)

    /**
     * Restores a deleted music.
     *
     * @param musicId Id of the music.
     * @return How many music were changed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun musicRestore(musicId: String): AppFunctionChangedCount = musicAppFunction.restore(musicId = musicId)

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
    ): AppFunctionMemoPage = searchAppFunction.memo(query = query, sort = sort, cursor = cursor)

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
    ): AppFunctionSearchTagPage = searchAppFunction.tag(query = query, sort = sort, cursor = cursor)

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
    ): AppFunctionSearchPlacePage = searchAppFunction.place(query = query, sort = sort, cursor = cursor)

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
    ): AppFunctionSearchWebPage = searchAppFunction.web(query = query, sort = sort, cursor = cursor)

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
    ): List<AppFunctionCalendarMemo> = calendarAppFunction.memoList(startDate = startDate, endDate = endDate)

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
    ): List<AppFunctionCalendarBirthday> = calendarAppFunction.birthdayList(startDate = startDate, endDate = endDate)

    /**
     * Lists the holidays shown on the calendar for a year, for the holiday countries the user chose and without the holidays the user hid. isHoliday is false for named days that are not days off. Only years already saved on this device are returned.
     *
     * @param year Year like 2026.
     * @return Holidays shown on the calendar.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    public suspend fun holidayList(year: Int): List<AppFunctionHoliday> = calendarAppFunction.holidayList(year = year)

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
    ): List<AppFunctionLunarDate> = lunarAppFunction.list(startDate = startDate, endDate = endDate)

    // endregion
}
