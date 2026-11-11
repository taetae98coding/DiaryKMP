package io.github.taetae98coding.diary.work.appaction

import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistItemUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FindMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.checklistAppActionOf
import io.github.taetae98coding.diary.work.appaction.checklist.checklistItemAppActionOf
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.contact.contactAppActionOf
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.dday.ddayAppActionOf
import io.github.taetae98coding.diary.work.appaction.memo.memoAppActionOf
import io.github.taetae98coding.diary.work.appaction.memo.memoListAppActionOf
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction
import io.github.taetae98coding.diary.work.appaction.memolink.memoLinkAppActionOf
import io.github.taetae98coding.diary.work.appaction.music.musicAppActionOf
import io.github.taetae98coding.diary.work.appaction.place.placeAppActionOf
import io.github.taetae98coding.diary.work.appaction.qr.qrAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.tagAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.tagItemAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.tagLinkAppActionOf
import io.github.taetae98coding.diary.work.appaction.web.webAppActionOf
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class AppActionDeletedTargetTest :
    FunSpec({
        test("TC-62354a1e-7c25-4d54-81d6-c705399ad890 삭제된 항목을 바꾸는 요청은 없는 항목과 같은 결과를 받는다") {
            runTest {
                changeCaseList().forEach { case ->
                    withClue(case.name) {
                        val deletedResult = case.result(TargetState.DELETED)

                        deletedResult shouldBe case.result(TargetState.MISSING)
                        deletedResult shouldBe case.expected
                    }
                }
            }
        }

        test("TC-a422baa4-340c-4bb5-9453-15232dc401dc 없거나 삭제된 항목의 연결과 해제는 바뀐 것이 없다는 결과를 받는다") {
            runTest {
                linkCaseList().forEach { case ->
                    withClue(case.name) {
                        val deletedResult = case.result(TargetState.DELETED)

                        deletedResult shouldBe case.result(TargetState.MISSING)
                        deletedResult shouldBe case.expected
                    }
                }
            }
        }

        test("TC-937bb971-5a62-49cc-abb7-f1b6f76661df 없거나 삭제된 체크리스트의 빈 항목 추가는 추가한 수 0을 받는다") {
            runTest {
                TargetState.entries.forEach { state ->
                    val checklist = checklistTarget(state)

                    checklistItemAppActionOf(checklist.findUseCase).add(checklistId = checklist.id, content = "") shouldBe unchanged
                }
            }
        }

        test("TC-aa3b2187-2072-4bf3-b79d-5c1cd17e889d 삭제된 항목에 딸린 목록을 요청하면 없는 항목과 같은 결과를 받는다") {
            runTest {
                listCaseList().forEach { case ->
                    withClue(case.name) {
                        val deletedResult = case.result(TargetState.DELETED)

                        deletedResult shouldBe case.result(TargetState.MISSING)
                        deletedResult shouldBe AppActionPage<Any>(items = emptyList(), nextCursor = null)
                    }
                }
            }
        }
    })

private enum class TargetState {
    DELETED,
    MISSING,
}

// 요청이 실패로 끝나면 실패 메시지를 결과로 삼아, 삭제된 항목과 없는 항목의 결과를 같은 방식으로 견준다.
private class TargetCase(
    val name: String,
    val expected: Any = unchanged,
    private val request: suspend (TargetState) -> Any?,
) {
    suspend fun result(state: TargetState): Any? =
        try {
            request(state)
        } catch (exception: AppActionException) {
            exception.message
        }
}

private val unchanged: AppActionChangedCount = AppActionChangedCount(changedCount = 0)

private val notDone: AppActionDone = AppActionDone(done = false)

private class Target<T : Any>(
    val id: String,
    val findUseCase: T,
)

// 조회 기능만 답하게 하고 나머지는 답이 없는 mock으로 두어, 삭제된 항목에 바꾸기나 목록 기능이 실행되면 요청이 실패하게 한다.
private inline fun <reified T : FlowUseCase<Uuid, V?>, V : Any> target(
    state: TargetState,
    id: Uuid,
    deleted: V,
): Target<T> {
    val findUseCase = mockk<T>()
    every { findUseCase(parameter = id) } returns flowOf(Result.success(deleted.takeIf { state == TargetState.DELETED }))

    return Target(id = id.toString(), findUseCase = findUseCase)
}

private fun memoTarget(state: TargetState): Target<FindMemoUseCase> =
    Arb
        .memo()
        .next()
        .copy(isDeleted = true)
        .let { memo -> target(state = state, id = memo.id, deleted = memo) }

private fun tagTarget(state: TargetState): Target<FindTagUseCase> =
    Arb
        .tag()
        .next()
        .copy(isDeleted = true)
        .let { tag -> target(state = state, id = tag.id, deleted = tag) }

private fun placeTarget(state: TargetState): Target<FindPlaceUseCase> =
    Arb
        .place()
        .next()
        .copy(isDeleted = true)
        .let { place -> target(state = state, id = place.id, deleted = place) }

private fun webTarget(state: TargetState): Target<FindWebUseCase> =
    Arb
        .web()
        .next()
        .copy(isDeleted = true)
        .let { web -> target(state = state, id = web.id, deleted = web) }

private fun contactTarget(state: TargetState): Target<FindContactUseCase> =
    Arb
        .contact()
        .next()
        .copy(isDeleted = true)
        .let { contact -> target(state = state, id = contact.id, deleted = contact) }

private fun checklistTarget(state: TargetState): Target<FindChecklistUseCase> =
    Arb
        .checklist()
        .next()
        .copy(isDeleted = true)
        .let { checklist -> target(state = state, id = checklist.id, deleted = checklist) }

private fun qrTarget(state: TargetState): Target<FindQrUseCase> =
    Arb
        .qr()
        .next()
        .copy(isDeleted = true)
        .let { qr -> target(state = state, id = qr.id, deleted = qr) }

private fun musicTarget(state: TargetState): Target<FindMusicUseCase> =
    Arb
        .music()
        .next()
        .copy(isDeleted = true)
        .let { music -> target(state = state, id = music.id, deleted = music) }

private fun ddayTarget(state: TargetState): Target<FindDDayUseCase> =
    Arb
        .dday()
        .next()
        .copy(isDeleted = true)
        .let { dday -> target(state = state, id = dday.id, deleted = dday) }

private fun changeCaseList(): List<TargetCase> = memoChangeCaseList() + tagChangeCaseList() + linkedEntityChangeCaseList() + contactChangeCaseList() + checklistChangeCaseList() + taggedEntityChangeCaseList()

private fun memoChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "메모 수정") { state ->
            val memo = memoTarget(state)
            memoAppActionOf(memo.findUseCase).update(memoId = memo.id, title = null, description = null, color = null, dateTime = null, clearDateTime = null)
        },
        TargetCase(name = "메모 복사", expected = "IllegalArgumentException: Required value was null.") { state ->
            val memo = memoTarget(state)
            memoAppActionOf(memo.findUseCase).copy(memoId = memo.id)
        },
        TargetCase(name = "메모 완료") { state -> memoTarget(state).let { memo -> memoAppActionOf(memo.findUseCase).finish(memoId = memo.id) } },
        TargetCase(name = "메모 다시 시작") { state -> memoTarget(state).let { memo -> memoAppActionOf(memo.findUseCase).restart(memoId = memo.id) } },
        TargetCase(name = "메모 삭제") { state -> memoTarget(state).let { memo -> memoAppActionOf(memo.findUseCase).delete(memoId = memo.id) } },
    )

private fun tagChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "태그 수정") { state ->
            val tag = tagTarget(state)
            tagAppActionOf(tag.findUseCase).update(tagId = tag.id, emoji = null, title = null, description = null, color = null)
        },
        TargetCase(name = "태그 완료") { state -> tagTarget(state).let { tag -> tagAppActionOf(tag.findUseCase).finish(tagId = tag.id) } },
        TargetCase(name = "태그 다시 시작") { state -> tagTarget(state).let { tag -> tagAppActionOf(tag.findUseCase).restart(tagId = tag.id) } },
        TargetCase(name = "태그 삭제") { state -> tagTarget(state).let { tag -> tagAppActionOf(tag.findUseCase).delete(tagId = tag.id) } },
    )

private fun linkedEntityChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "장소 수정") { state ->
            val place = placeTarget(state)
            placeAppActionOf(place.findUseCase).update(placeId = place.id, title = null, description = null, color = null, address = null, latitude = null, longitude = null)
        },
        TargetCase(name = "장소 삭제") { state -> placeTarget(state).let { place -> placeAppActionOf(place.findUseCase).delete(placeId = place.id) } },
        TargetCase(name = "웹 수정") { state ->
            val web = webTarget(state)
            webAppActionOf(web.findUseCase).update(webId = web.id, title = null, description = null, url = null, headers = null)
        },
        TargetCase(name = "웹 삭제") { state -> webTarget(state).let { web -> webAppActionOf(web.findUseCase).delete(webId = web.id) } },
    )

private fun contactChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "연락처 수정") { state ->
            val contact = contactTarget(state)
            contactAppActionOf(contact.findUseCase).update(input = AppActionContactUpdateInput(contactId = contact.id))
        },
        TargetCase(name = "연락처 즐겨찾기") { state -> contactTarget(state).let { contact -> contactAppActionOf(contact.findUseCase).favorite(contactId = contact.id) } },
        TargetCase(name = "연락처 즐겨찾기 해제") { state -> contactTarget(state).let { contact -> contactAppActionOf(contact.findUseCase).unfavorite(contactId = contact.id) } },
        TargetCase(name = "연락처 삭제") { state -> contactTarget(state).let { contact -> contactAppActionOf(contact.findUseCase).delete(contactId = contact.id) } },
    )

private fun checklistChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "체크리스트 수정") { state ->
            val checklist = checklistTarget(state)
            checklistAppActionOf(checklist.findUseCase).update(checklistId = checklist.id, title = null, description = null)
        },
        TargetCase(name = "체크리스트 항목 추가") { state ->
            val checklist = checklistTarget(state)
            checklistItemAppActionOf(checklist.findUseCase).add(checklistId = checklist.id, content = Arb.nonBlankString().next())
        },
        TargetCase(name = "체크리스트 항목 체크") { state -> checklistItemTarget(state).let { item -> item.action.check(itemId = item.id) } },
        TargetCase(name = "체크리스트 항목 체크 해제") { state -> checklistItemTarget(state).let { item -> item.action.uncheck(itemId = item.id) } },
        TargetCase(name = "체크리스트 항목 삭제") { state -> checklistItemTarget(state).let { item -> item.action.delete(itemId = item.id) } },
        TargetCase(name = "삭제된 체크리스트 항목 체크") { state -> deletedChecklistItemTarget(state).let { item -> item.action.check(itemId = item.id) } },
        TargetCase(name = "삭제된 체크리스트 항목 체크 해제") { state -> deletedChecklistItemTarget(state).let { item -> item.action.uncheck(itemId = item.id) } },
        TargetCase(name = "삭제된 체크리스트 항목 삭제") { state -> deletedChecklistItemTarget(state).let { item -> item.action.delete(itemId = item.id) } },
        TargetCase(name = "체크리스트 체크 모두 해제") { state -> checklistTarget(state).let { checklist -> checklistAppActionOf(checklist.findUseCase).reset(checklistId = checklist.id) } },
        TargetCase(name = "체크리스트 삭제") { state -> checklistTarget(state).let { checklist -> checklistAppActionOf(checklist.findUseCase).delete(checklistId = checklist.id) } },
    )

private class ChecklistItemTarget(
    val id: String,
    val action: ChecklistItemAppAction,
)

// 항목은 삭제되지 않은 채 저장되어 있고, 그 항목이 속한 체크리스트만 삭제되었거나 없다.
private fun checklistItemTarget(state: TargetState): ChecklistItemTarget {
    val checklist = checklistTarget(state)
    val item = Arb.checklistItem(isChecked = false, isDeleted = false, checklistId = Uuid.parse(checklist.id)).next()
    val findChecklistItemUseCase = mockk<FindChecklistItemUseCase>()
    every { findChecklistItemUseCase(parameter = item.id) } returns flowOf(Result.success(item))

    return ChecklistItemTarget(id = item.id.toString(), action = checklistItemAppActionOf(checklist.findUseCase, findChecklistItemUseCase))
}

// 체크리스트는 삭제되지 않은 채 저장되어 있고, 항목만 삭제되었거나 없다.
private fun deletedChecklistItemTarget(state: TargetState): ChecklistItemTarget {
    val item = Arb.checklistItem(isChecked = false, isDeleted = true).next()
    val target = target<FindChecklistItemUseCase, _>(state = state, id = item.id, deleted = item)

    return ChecklistItemTarget(id = target.id, action = checklistItemAppActionOf(target.findUseCase))
}

private fun taggedEntityChangeCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "QR 수정") { state -> qrTarget(state).let { qr -> qrAppActionOf(qr.findUseCase).update(qrId = qr.id, title = null, description = null, value = null) } },
        TargetCase(name = "QR 삭제") { state -> qrTarget(state).let { qr -> qrAppActionOf(qr.findUseCase).delete(qrId = qr.id) } },
        TargetCase(name = "곡 수정") { state -> musicTarget(state).let { music -> musicAppActionOf(music.findUseCase).update(musicId = music.id, title = null, artist = null, link = null) } },
        TargetCase(name = "곡 삭제") { state -> musicTarget(state).let { music -> musicAppActionOf(music.findUseCase).delete(musicId = music.id) } },
        TargetCase(name = "디데이 수정") { state ->
            val dday = ddayTarget(state)
            ddayAppActionOf(dday.findUseCase).update(
                ddayId = dday.id,
                title = null,
                description = null,
                color = null,
                baseDate = null,
                countsBaseDay = null,
                repeats = null,
                endDate = null,
                clearEndDate = null,
                overflowPolicy = null,
            )
        },
        TargetCase(name = "디데이 삭제") { state -> ddayTarget(state).let { dday -> ddayAppActionOf(dday.findUseCase).delete(ddayId = dday.id) } },
    )

private fun linkCaseList(): List<TargetCase> = memoLinkCaseList() + entityTagLinkCaseList() + tagLinkCaseList()

// 지정한 두 항목 가운데 한쪽만 삭제되었거나 없고, 다른 쪽은 삭제되지 않은 채 저장되어 있다.
private fun memoLinkCaseList(): List<TargetCase> =
    memoLinkCase(name = "메모 태그 연결", target = ::tagTarget) { action, memoId, tagId -> action.tagAdd(memoId = memoId, tagId = tagId) } +
        memoLinkCase(name = "메모 태그 연결 해제", target = ::tagTarget) { action, memoId, tagId -> action.tagRemove(memoId = memoId, tagId = tagId) } +
        memoLinkCase(name = "메모 대표 태그 지정", target = ::tagTarget) { action, memoId, tagId -> action.primaryTagSet(memoId = memoId, tagId = tagId) } +
        memoLinkCase(name = "메모 장소 연결", target = ::placeTarget) { action, memoId, placeId -> action.placeAdd(memoId = memoId, placeId = placeId) } +
        memoLinkCase(name = "메모 장소 연결 해제", target = ::placeTarget) { action, memoId, placeId -> action.placeRemove(memoId = memoId, placeId = placeId) } +
        memoLinkCase(name = "메모 웹 연결", target = ::webTarget) { action, memoId, webId -> action.webAdd(memoId = memoId, webId = webId) } +
        memoLinkCase(name = "메모 웹 연결 해제", target = ::webTarget) { action, memoId, webId -> action.webRemove(memoId = memoId, webId = webId) } +
        memoLinkCase(name = "메모 연락처 연결", target = ::contactTarget) { action, memoId, contactId -> action.contactAdd(memoId = memoId, contactId = contactId) } +
        memoLinkCase(name = "메모 연락처 연결 해제", target = ::contactTarget) { action, memoId, contactId -> action.contactRemove(memoId = memoId, contactId = contactId) } +
        TargetCase(name = "메모 대표 태그 해제", expected = notDone) { state -> memoTarget(state).let { memo -> memoLinkAppActionOf(memo.findUseCase).primaryTagUnset(memoId = memo.id) } }

private fun memoLinkCase(
    name: String,
    target: (TargetState) -> Target<*>,
    request: suspend (action: MemoLinkAppAction, memoId: String, targetId: String) -> Any,
): List<TargetCase> =
    listOf(
        TargetCase(name = "$name: 메모", expected = notDone) { state -> memoTarget(state).let { memo -> request(memoLinkAppActionOf(memo.findUseCase), memo.id, activeId()) } },
        TargetCase(name = "$name: 연결 대상", expected = notDone) { state -> target(state).let { linked -> request(memoLinkAppActionOf(linked.findUseCase), activeId(), linked.id) } },
    )

private fun entityTagLinkCaseList(): List<TargetCase> =
    listOf<Pair<AppActionEntityTagKind, (TargetState) -> Target<*>>>(
        AppActionEntityTagKind.PLACE to ::placeTarget,
        AppActionEntityTagKind.WEB to ::webTarget,
        AppActionEntityTagKind.CONTACT to ::contactTarget,
        AppActionEntityTagKind.CHECKLIST to ::checklistTarget,
        AppActionEntityTagKind.QR to ::qrTarget,
        AppActionEntityTagKind.MUSIC to ::musicTarget,
        AppActionEntityTagKind.DDAY to ::ddayTarget,
    ).flatMap { (kind, target) ->
        listOf(
            TargetCase(name = "$kind 태그 연결: 항목", expected = notDone) { state -> target(state).let { entity -> entityTagAppActionOf(entity.findUseCase).add(kind = kind, entityId = entity.id, tagId = activeId()) } },
            TargetCase(name = "$kind 태그 연결 해제: 항목", expected = notDone) { state -> target(state).let { entity -> entityTagAppActionOf(entity.findUseCase).remove(kind = kind, entityId = entity.id, tagId = activeId()) } },
            TargetCase(name = "$kind 태그 연결: 태그", expected = notDone) { state -> tagTarget(state).let { tag -> entityTagAppActionOf(tag.findUseCase).add(kind = kind, entityId = activeId(), tagId = tag.id) } },
            TargetCase(name = "$kind 태그 연결 해제: 태그", expected = notDone) { state -> tagTarget(state).let { tag -> entityTagAppActionOf(tag.findUseCase).remove(kind = kind, entityId = activeId(), tagId = tag.id) } },
        )
    }

private fun tagLinkCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "태그 연결: 태그", expected = notDone) { state -> tagLinkTarget(state).let { link -> link.action.add(tagId = link.targetId, linkedTagId = link.activeId) } },
        TargetCase(name = "태그 연결: 하위 태그", expected = notDone) { state -> tagLinkTarget(state).let { link -> link.action.add(tagId = link.activeId, linkedTagId = link.targetId) } },
        TargetCase(name = "태그 연결 해제: 태그", expected = notDone) { state -> tagLinkTarget(state).let { link -> link.action.remove(tagId = link.targetId, linkedTagId = link.activeId) } },
        TargetCase(name = "태그 연결 해제: 하위 태그", expected = notDone) { state -> tagLinkTarget(state).let { link -> link.action.remove(tagId = link.activeId, linkedTagId = link.targetId) } },
    )

private class TagLinkTarget(
    val targetId: String,
    val activeId: String,
    val action: TagLinkAppAction,
)

private fun tagLinkTarget(state: TargetState): TagLinkTarget {
    val target = tagTarget(state)
    val active = Arb.tag().next().copy(isDeleted = false)
    every { target.findUseCase(parameter = active.id) } returns flowOf(Result.success(active))

    return TagLinkTarget(targetId = target.id, activeId = active.id.toString(), action = tagLinkAppActionOf(target.findUseCase))
}

private fun activeId(): String = Arb.kotlinUuid().next().toString()

private fun listCaseList(): List<TargetCase> = tagListCaseList() + memoListCaseList()

private fun tagListCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "태그의 진행 중 메모") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).memoList(tagId = tag.id, scope = null, sort = null, cursor = null) } },
        TargetCase(name = "태그의 완료된 메모") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).memoFinishedList(tagId = tag.id, sort = null, cursor = null) } },
        TargetCase(name = "태그의 웹") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).webList(tagId = tag.id, scope = null, sort = null, cursor = null) } },
        TargetCase(name = "태그의 장소") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).placeList(tagId = tag.id, scope = null, sort = null, cursor = null) } },
        TargetCase(name = "태그의 연락처") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).contactList(tagId = tag.id, scope = null, sort = null, cursor = null) } },
        TargetCase(name = "태그의 체크리스트") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).checklistList(tagId = tag.id, scope = null, cursor = null) } },
        TargetCase(name = "태그의 곡") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).musicList(tagId = tag.id, scope = null, sort = null, cursor = null) } },
        TargetCase(name = "태그의 QR") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).qrList(tagId = tag.id, scope = null, cursor = null) } },
        TargetCase(name = "태그의 디데이") { state -> tagTarget(state).let { tag -> tagItemAppActionOf(tag.findUseCase).ddayList(tagId = tag.id, scope = null, cursor = null) } },
    )

private fun memoListCaseList(): List<TargetCase> =
    listOf(
        TargetCase(name = "장소의 진행 중 메모") { state -> placeTarget(state).let { place -> memoListAppActionOf(place.findUseCase).placeList(placeId = place.id, sort = null, cursor = null) } },
        TargetCase(name = "장소의 완료된 메모") { state -> placeTarget(state).let { place -> memoListAppActionOf(place.findUseCase).placeFinishedList(placeId = place.id, sort = null, cursor = null) } },
        TargetCase(name = "웹의 진행 중 메모") { state -> webTarget(state).let { web -> memoListAppActionOf(web.findUseCase).webList(webId = web.id, sort = null, cursor = null) } },
        TargetCase(name = "웹의 완료된 메모") { state -> webTarget(state).let { web -> memoListAppActionOf(web.findUseCase).webFinishedList(webId = web.id, sort = null, cursor = null) } },
        TargetCase(name = "연락처의 진행 중 메모") { state -> contactTarget(state).let { contact -> memoListAppActionOf(contact.findUseCase).contactList(contactId = contact.id, sort = null, cursor = null) } },
        TargetCase(name = "연락처의 완료된 메모") { state -> contactTarget(state).let { contact -> memoListAppActionOf(contact.findUseCase).contactFinishedList(contactId = contact.id, sort = null, cursor = null) } },
    )
