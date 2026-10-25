package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.coVerify
import kotlin.uuid.Uuid

class SyncWorkChecklistTest :
    FunSpec({
        test("TC-28a7ff40-be46-4362-a4de-ddf85ceba185 체크리스트만 대기하면 체크리스트 요청만 발생한다") {
            val context = context(checklistList = checklists(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.checklistRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.checklistItemRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }

        test("TC-28a7ff40-be46-4362-a4de-ddf85ceba185 체크 항목만 대기하면 체크 항목 요청만 발생한다") {
            val context = context(checklistItemList = checklistItems(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.checklistItemRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.checklistRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 체크리스트 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val checklistList = checklists(size = itemCount)
                val context = context(checklistList = checklistList)
                val requests = mutableListOf<List<ChecklistRemoteEntity>>()
                coEvery { context.checklistRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<ChecklistRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly checklistList.map { checklist -> checklist.toRemote() }
            }

            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 체크 항목 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val itemList = checklistItems(size = itemCount)
                val context = context(checklistItemList = itemList)
                val requests = mutableListOf<List<ChecklistItemRemoteEntity>>()
                coEvery { context.checklistItemRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<ChecklistItemRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly itemList.map { item -> item.toRemote() }
            }
        }

        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 삭제된 체크리스트와 뺀 체크 항목도 삭제 상태로 업로드된다") {
            val checklistList = listOf(fixtureMonkey.localChecklist(isDeleted = true), fixtureMonkey.localChecklist(isDeleted = false))
            val itemList =
                listOf(
                    fixtureMonkey.localChecklistItem(isChecked = fixtureMonkey.giveMeOne(), isDeleted = true),
                    fixtureMonkey.localChecklistItem(isChecked = fixtureMonkey.giveMeOne(), isDeleted = false),
                )
            val context = context(checklistList = checklistList, checklistItemList = itemList)
            val checklistRequests = mutableListOf<List<ChecklistRemoteEntity>>()
            val itemRequests = mutableListOf<List<ChecklistItemRemoteEntity>>()
            coEvery { context.checklistRemoteDataSource.push(any()) } coAnswers {
                checklistRequests += firstArg<List<ChecklistRemoteEntity>>()
            }
            coEvery { context.checklistItemRemoteDataSource.push(any()) } coAnswers {
                itemRequests += firstArg<List<ChecklistItemRemoteEntity>>()
            }

            context.subject.doWork()

            checklistRequests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
            itemRequests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-860568ea-3f66-4d2d-833d-4eeeed777358 같은 체크리스트의 서로 다른 체크 항목은 체크리스트 없이 각자의 전체 상태로 업로드된다") {
            val checklistId = fixtureMonkey.giveMeOne<Uuid>()
            val itemList =
                listOf(
                    fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklistId),
                    fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklistId),
                )
            val context = context(checklistItemList = itemList)
            val requests = mutableListOf<List<ChecklistItemRemoteEntity>>()
            coEvery { context.checklistItemRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<ChecklistItemRemoteEntity>>()
            }

            context.subject.doWork()

            coVerify(exactly = 0) { context.checklistRemoteDataSource.push(any()) }
            requests.flatten() shouldContainExactly itemList.map { item -> item.toRemote() }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드 묶음이 성공하면 보낸 체크리스트와 체크 항목의 대기 해제를 요청한다") {
            val checklistList = checklists(size = 101)
            val itemList = checklistItems(size = 3)
            val context = context(checklistList = checklistList, checklistItemList = itemList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountChecklistSyncTransaction.clearPending(accountId = context.accountId, checklistList = checklistList.take(100))
            }
            coVerify(exactly = 1) {
                context.accountChecklistSyncTransaction.clearPending(accountId = context.accountId, checklistList = checklistList.drop(100))
            }
            coVerify(exactly = 1) {
                context.accountChecklistItemSyncTransaction.clearPending(accountId = context.accountId, itemList = itemList)
            }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee TC-90561386-15a3-4139-9983-cb88f3fe4a47 체크 항목 업로드가 실패하면 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
            val context = context(checklistItemList = checklistItems(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.checklistItemRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 0) { context.accountChecklistItemSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.checklistItemRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.checklistRemoteDataSource.pull(any()) }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee TC-90561386-15a3-4139-9983-cb88f3fe4a47 체크리스트 업로드가 실패하면 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
            val context = context(checklistList = checklists(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.checklistRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 0) { context.accountChecklistSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.checklistRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-c320d931-4f62-4f3c-a2e8-ebae4742a3c3 체크리스트 업로드가 성공하고 체크 항목 업로드가 실패하면 체크리스트의 대기 해제만 유지된다") {
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val item = fixtureMonkey.localChecklistItem(isChecked = false, isDeleted = false, checklistId = checklist.id)
            val context = context(checklistList = listOf(checklist), checklistItemList = listOf(item))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.checklistItemRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 1) { context.accountChecklistSyncTransaction.clearPending(accountId = context.accountId, checklistList = listOf(checklist)) }
            coVerify(exactly = 0) { context.accountChecklistItemSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.checklistRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.checklistItemRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그 업로드가 실패해도 체크리스트와 체크 항목은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    checklistList = checklists(size = 201),
                    checklistItemList = checklistItems(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 3) { context.checklistRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.checklistItemRemoteDataSource.push(any()) }
        }

        listOf(
            "체크리스트" to true,
            "체크 항목" to false,
        ).forEach { (failingLabel, isChecklistFailing) ->
            test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 $failingLabel 이 실패해도 태그와 장소와 웹 항목과 연락처와 곡과 QR은 끝까지 업로드한다") {
                val context =
                    context(
                        tagList = tags(size = 201),
                        placeList = places(size = 201),
                        webList = webs(size = 201),
                        contactList = contacts(size = 201),
                        musicList = musics(size = 201),
                        qrList = qrs(size = 201),
                        checklistList = checklists(size = 1),
                        checklistItemList = checklistItems(size = 1),
                    )
                val failure = TestException(fixtureMonkey.giveMeOne())
                val requestMap = mutableMapOf<String, MutableList<Int>>()
                if (isChecklistFailing) {
                    coEvery { context.checklistRemoteDataSource.push(any()) } throws failure
                } else {
                    coEvery { context.checklistItemRemoteDataSource.push(any()) } throws failure
                }
                coEvery { context.tagRemoteDataSource.push(any()) } answers { requestMap.getOrPut("tag") { mutableListOf() } += firstArg<List<Any>>().size }
                coEvery { context.placeRemoteDataSource.push(any()) } answers { requestMap.getOrPut("place") { mutableListOf() } += firstArg<List<Any>>().size }
                coEvery { context.webRemoteDataSource.push(any()) } answers { requestMap.getOrPut("web") { mutableListOf() } += firstArg<List<Any>>().size }
                coEvery { context.contactRemoteDataSource.push(any()) } answers { requestMap.getOrPut("contact") { mutableListOf() } += firstArg<List<Any>>().size }
                coEvery { context.musicRemoteDataSource.push(any()) } answers { requestMap.getOrPut("music") { mutableListOf() } += firstArg<List<Any>>().size }
                coEvery { context.qrRemoteDataSource.push(any()) } answers { requestMap.getOrPut("qr") { mutableListOf() } += firstArg<List<Any>>().size }

                shouldThrowExactly<TestException> { context.subject.doWork() }

                listOf("tag", "place", "web", "contact", "music", "qr").forEach { key ->
                    requestMap[key].orEmpty() shouldContainExactly listOf(100, 100, 1)
                }
            }
        }

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 체크리스트와 체크 항목 내려받기는 각자 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val checklistPullList = checklistPulls(usnList = listOf(4L))
            val itemPullList = checklistItemPulls(usnList = listOf(7L, 8L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.CHECKLIST) } returns 2L
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.CHECKLIST_ITEM) } returns 5L
            coEvery { context.checklistRemoteDataSource.pull(usn = 2L) } returns checklistPullList
            coEvery { context.checklistRemoteDataSource.pull(usn = 4L) } returns emptyList()
            coEvery { context.checklistItemRemoteDataSource.pull(usn = 5L) } returns itemPullList
            coEvery { context.checklistItemRemoteDataSource.pull(usn = 8L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountChecklistSyncTransaction.upsert(
                    accountId = context.accountId,
                    checklistList = checklistPullList.map { pull -> pull.checklist.toLocal() },
                    cursor = 4L,
                )
            }
            coVerify(exactly = 1) {
                context.accountChecklistItemSyncTransaction.upsert(
                    accountId = context.accountId,
                    itemList = itemPullList.map { pull -> pull.checklistItem.toLocal() },
                    cursor = 8L,
                )
            }
            coVerify(exactly = 2) { context.checklistRemoteDataSource.pull(any()) }
            coVerify(exactly = 2) { context.checklistItemRemoteDataSource.pull(any()) }
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 체크리스트와 체크 항목만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery { context.checklistSyncLocalDataSource.readPendingList(accountId = otherAccountId) } returns checklists(size = 1)
            coEvery { context.checklistItemSyncLocalDataSource.readPendingList(accountId = otherAccountId) } returns checklistItems(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) { context.checklistSyncLocalDataSource.readPendingList(accountId = accountId) }
            coVerify(exactly = 1) { context.checklistItemSyncLocalDataSource.readPendingList(accountId = accountId) }
            coVerify(exactly = 0) { context.checklistSyncLocalDataSource.readPendingList(accountId = otherAccountId) }
            coVerify(exactly = 0) { context.checklistItemSyncLocalDataSource.readPendingList(accountId = otherAccountId) }
            coVerify(exactly = 0) { context.checklistRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.checklistItemRemoteDataSource.push(any()) }
        }
    })
