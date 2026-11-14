package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

// 여덟 종류의 항목과 태그의 연결을 같은 방식으로 검증하도록 종류마다 항목의 요청과 문서의 케이스를 묶는다.
private class EntityTagCase(
    val label: String,
    val kind: EntityTagKindLocalEntity,
    val remoteKind: EntityTagKindRemoteEntity,
    val separateUploadTcId: String,
    val deletedUploadTcId: String,
    val pullTcId: String,
    val entityFailureTcId: String,
    val entityPush: suspend MockKMatcherScope.(TestContext) -> Unit,
    val entityPull: suspend MockKMatcherScope.(TestContext, Long) -> List<Any>,
)

private val entityTagCaseList: List<EntityTagCase> =
    listOf(
        EntityTagCase(
            label = "웹 항목",
            kind = EntityTagKindLocalEntity.WEB,
            remoteKind = EntityTagKindRemoteEntity.WEB,
            separateUploadTcId = "TC-fbebf0be-a83a-43c9-8c7b-0c47f25e1e00",
            deletedUploadTcId = "TC-fb383a98-cf0e-4086-af3d-4e557b8edd75",
            pullTcId = "TC-3de3f780-f097-4009-b2ff-da214d0d3328",
            entityFailureTcId = "TC-98a227dd-770f-4c9a-9c31-1602b3c77614",
            entityPush = { context -> context.webRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.webRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "장소",
            kind = EntityTagKindLocalEntity.PLACE,
            remoteKind = EntityTagKindRemoteEntity.PLACE,
            separateUploadTcId = "TC-7304e71b-b36f-423d-9efa-52d3d75bcdd9",
            deletedUploadTcId = "TC-a7ac9999-59d1-445b-bc5b-312a87bad444",
            pullTcId = "TC-96b8e102-0188-48ac-83f6-9f2651aaee11",
            entityFailureTcId = "TC-8d545576-a91d-40e3-9faa-e18821fa4c27",
            entityPush = { context -> context.placeRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.placeRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "연락처",
            kind = EntityTagKindLocalEntity.CONTACT,
            remoteKind = EntityTagKindRemoteEntity.CONTACT,
            separateUploadTcId = "TC-7116fdba-725f-490e-ab90-4d6772791a88",
            deletedUploadTcId = "TC-f5d7255d-3752-4f7d-8c5e-882b71fa516e",
            pullTcId = "TC-bf88c8cb-9494-4bf7-a207-bc4d48a1fdb7",
            entityFailureTcId = "TC-98a227dd-770f-4c9a-9c31-1602b3c77614",
            entityPush = { context -> context.contactRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.contactRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "체크리스트",
            kind = EntityTagKindLocalEntity.CHECKLIST,
            remoteKind = EntityTagKindRemoteEntity.CHECKLIST,
            separateUploadTcId = "TC-88450497-a69a-4738-b91b-558fa1d00cda",
            deletedUploadTcId = "TC-c2ec35b0-3f18-47d6-89cb-685728ff2fb6",
            pullTcId = "TC-a6fd5fa8-6296-445d-b8ac-7d06615f38db",
            entityFailureTcId = "TC-4883afb2-10a2-4f67-a825-933299a7197a",
            entityPush = { context -> context.checklistRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.checklistRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "곡",
            kind = EntityTagKindLocalEntity.MUSIC,
            remoteKind = EntityTagKindRemoteEntity.MUSIC,
            separateUploadTcId = "TC-f0ac2072-ec83-435d-a33e-63afe03d825f",
            deletedUploadTcId = "TC-ffb0dac8-27ef-4c74-b9d6-78f632e07694",
            pullTcId = "TC-08270649-c0fd-4fa9-beff-db394a7c6f30",
            entityFailureTcId = "TC-bbb82d9b-1756-4aac-8be5-71f1e3903825",
            entityPush = { context -> context.musicRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.musicRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "QR",
            kind = EntityTagKindLocalEntity.QR,
            remoteKind = EntityTagKindRemoteEntity.QR,
            separateUploadTcId = "TC-f7ef8c7b-8bd9-4627-a510-fb09b2099345",
            deletedUploadTcId = "TC-7480fbc5-d840-49ae-af4a-918f9ce3ccd2",
            pullTcId = "TC-e6e6a0ab-99b7-446a-b6d5-0d03d0372fe1",
            entityFailureTcId = "TC-bbb82d9b-1756-4aac-8be5-71f1e3903825",
            entityPush = { context -> context.qrRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.qrRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "디데이",
            kind = EntityTagKindLocalEntity.DDAY,
            remoteKind = EntityTagKindRemoteEntity.DDAY,
            separateUploadTcId = "TC-625e9cc2-5758-408e-b2cc-3848aced64e9",
            deletedUploadTcId = "TC-e3c79a10-0f67-404b-9a01-c528f91912af",
            pullTcId = "TC-5540aae3-5d58-4bcb-a8cf-7f74047a738d",
            entityFailureTcId = "TC-bbb82d9b-1756-4aac-8be5-71f1e3903825",
            entityPush = { context -> context.ddayRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.ddayRemoteDataSource.pull(usn = usn) },
        ),
        EntityTagCase(
            label = "루틴",
            kind = EntityTagKindLocalEntity.ROUTINE,
            remoteKind = EntityTagKindRemoteEntity.ROUTINE,
            separateUploadTcId = "TC-ed75f3dc-6b86-4679-8f6e-82ae9fe34b20",
            deletedUploadTcId = "TC-8a1cf22f-fe8b-48c2-b48b-33754b15754e",
            pullTcId = "TC-b47ae447-c72a-4d42-a319-d644676cf2c9",
            entityFailureTcId = "TC-159f47b4-d880-4ef7-bf64-b0f4a7a4465d",
            entityPush = { context -> context.routineRemoteDataSource.push(any()) },
            entityPull = { context, usn -> context.routineRemoteDataSource.pull(usn = usn) },
        ),
    )

private fun EntityTagCase.context(
    accountId: Uuid = Arb.kotlinUuid().next(),
    tagCount: Int = 0,
    entityCount: Int = 0,
    entityTagList: List<EntityTagLocalEntity> = emptyList(),
    memoCount: Int = 0,
    memoTagCount: Int = 0,
): TestContext {
    fun count(target: EntityTagKindLocalEntity): Int = if (kind == target) entityCount else 0

    return context(
        accountId = accountId,
        tagList = tags(size = tagCount),
        placeList = places(size = count(EntityTagKindLocalEntity.PLACE)),
        webList = webs(size = count(EntityTagKindLocalEntity.WEB)),
        contactList = contacts(size = count(EntityTagKindLocalEntity.CONTACT)),
        checklistList = checklists(size = count(EntityTagKindLocalEntity.CHECKLIST)),
        musicList = musics(size = count(EntityTagKindLocalEntity.MUSIC)),
        qrList = qrs(size = count(EntityTagKindLocalEntity.QR)),
        ddayList = ddays(size = count(EntityTagKindLocalEntity.DDAY)),
        routineList = routines(size = count(EntityTagKindLocalEntity.ROUTINE)),
        memoList = memos(size = memoCount),
        memoTagList = memoTags(size = memoTagCount),
        entityTagMap = mapOf(kind to entityTagList),
    )
}

class SyncWorkEntityTagTest :
    FunSpec({
        entityTagCaseList.forEach { case ->
            val label = "${case.label}·태그 연결"

            test("TC-674eebce-610c-4b46-8dce-79aef5252327 ${case.separateUploadTcId} 모든 태그 요청과 ${case.label} 요청이 성공한 뒤 $label 전용 요청을 시작한다") {
                val context = case.context(tagCount = 101, entityCount = 101, entityTagList = entityTags(size = 101))
                val requestOrder = mutableListOf<String>()
                coEvery { context.tagRemoteDataSource.push(any()) } coAnswers { requestOrder += "tag" }
                coEvery { case.entityPush(this, context) } coAnswers { requestOrder += "entity" }
                coEvery { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) } coAnswers {
                    requestOrder += "entityTag"
                }

                context.subject.doWork()

                requestOrder.takeLast(2) shouldContainExactly listOf("entityTag", "entityTag")
                requestOrder.dropLast(2) shouldContainExactlyInAnyOrder listOf("tag", "tag", "entity", "entity")
            }

            test("$label 만 대기하면 $label 요청만 발생한다") {
                val context = case.context(entityTagList = entityTags(size = 1))

                context.subject.doWork()

                coVerify(exactly = 1) { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) }
                coVerify(exactly = 0) {
                    context.entityTagRemoteDataSource.push(kind = match { kind -> kind != case.remoteKind }, entityTagList = any())
                }
                coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
                coVerify(exactly = 0) { case.entityPush(this, context) }
                coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
            }

            listOf(
                1 to listOf(1),
                100 to listOf(100),
                201 to listOf(100, 100, 1),
            ).forEach { (itemCount, expectedRequestSizes) ->
                test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 $label $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                    val entityTagList = entityTags(size = itemCount)
                    val context = case.context(entityTagList = entityTagList)
                    val requests = mutableListOf<List<EntityTagRemoteEntity>>()
                    coEvery { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) } coAnswers {
                        requests += secondArg<List<EntityTagRemoteEntity>>()
                    }

                    context.subject.doWork()

                    requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                    requests.flatten() shouldContainExactly entityTagList.map { entityTag -> entityTag.toRemote() }
                }
            }

            test("${case.deletedUploadTcId} 해제된 $label 도 해제 상태로 업로드된다") {
                val entityTagList = listOf(entityTag(isDeleted = true), entityTag(isDeleted = false))
                val context = case.context(entityTagList = entityTagList)
                val requests = mutableListOf<List<EntityTagRemoteEntity>>()
                coEvery { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) } coAnswers {
                    requests += secondArg<List<EntityTagRemoteEntity>>()
                }

                context.subject.doWork()

                requests.flatten() shouldContainExactly entityTagList.map { entityTag -> entityTag.toRemote() }
                requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
            }

            test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 $label 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
                val entityTagList = entityTags(size = 101)
                val context = case.context(entityTagList = entityTagList)

                context.subject.doWork()

                coVerify(exactly = 1) {
                    context.accountEntityTagSyncTransaction.clearPending(
                        accountId = context.accountId,
                        kind = case.kind,
                        entityTagList = entityTagList.take(100),
                    )
                }
                coVerify(exactly = 1) {
                    context.accountEntityTagSyncTransaction.clearPending(
                        accountId = context.accountId,
                        kind = case.kind,
                        entityTagList = entityTagList.drop(100),
                    )
                }
            }

            test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee $label 업로드가 실패하면 내려받기를 진행하지 않는다") {
                val context = case.context(entityTagList = entityTags(size = 1))
                val failure = TestException(Arb.string().next())
                coEvery { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) } throws failure

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                coVerify(exactly = 0) { context.accountEntityTagSyncTransaction.clearPending(any(), any(), any()) }
                coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
                coVerify(exactly = 0) { context.entityTagRemoteDataSource.pull(any(), any()) }
            }

            test("TC-bb1f2746-1f2d-465a-8da5-35cff756be8c 태그 업로드가 실패하면 $label 을 시도하지 않는다") {
                val context = case.context(tagCount = 1, entityTagList = entityTags(size = 1))
                val failure = TestException(Arb.string().next())
                coEvery { context.tagRemoteDataSource.push(any()) } throws failure

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                coVerify(exactly = 0) { context.entityTagSyncLocalDataSource.readPendingList(accountId = any(), kind = case.kind) }
                coVerify(exactly = 0) { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) }
            }

            test("${case.entityFailureTcId} ${case.label} 묶음이 실패하면 $label 을 시도하지 않고 태그는 업로드한다") {
                val context = case.context(tagCount = 1, entityCount = 201, entityTagList = entityTags(size = 1))
                val failure = TestException(Arb.string().next())
                val entityRequestCount = mutableListOf<Int>()
                coEvery { case.entityPush(this, context) } coAnswers {
                    entityRequestCount += 1
                    if (entityRequestCount.size == 2) throw failure
                }

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                entityRequestCount.size shouldBe 2
                coVerify(exactly = 1) { context.tagRemoteDataSource.push(any()) }
                coVerify(exactly = 0) { context.entityTagSyncLocalDataSource.readPendingList(accountId = any(), kind = case.kind) }
                coVerify(exactly = 0) { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) }
            }

            test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 메모 업로드가 실패해도 $label 은 끝까지 업로드한다") {
                val context = case.context(tagCount = 1, memoCount = 201, entityTagList = entityTags(size = 201))
                val failure = TestException(Arb.string().next())
                val memoRequestCount = mutableListOf<Int>()
                coEvery { context.memoRemoteDataSource.push(any()) } coAnswers {
                    memoRequestCount += 1
                    if (memoRequestCount.size == 2) throw failure
                }

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                coVerify(exactly = 3) { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) }
            }

            test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 $label 업로드가 실패해도 메모와 메모·태그 연결은 끝까지 업로드한다") {
                val context = case.context(tagCount = 1, memoCount = 201, memoTagCount = 201, entityTagList = entityTags(size = 201))
                val failure = TestException(Arb.string().next())
                val entityTagRequestCount = mutableListOf<Int>()
                coEvery { context.entityTagRemoteDataSource.push(kind = case.remoteKind, entityTagList = any()) } coAnswers {
                    entityTagRequestCount += 1
                    if (entityTagRequestCount.size == 2) throw failure
                }

                shouldThrowExactly<TestException> {
                    context.subject.doWork()
                }

                entityTagRequestCount.size shouldBe 2
                coVerify(exactly = 3) { context.memoRemoteDataSource.push(any()) }
                coVerify(exactly = 3) { context.memoTagRemoteDataSource.push(any()) }
            }

            test("${case.pullTcId} $label 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
                val context = case.context()
                val firstPullList = entityTagPulls(usnList = listOf(4L, 6L))
                val secondPullList = entityTagPulls(usnList = listOf(9L))
                coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = case.kind.syncKind) } returns 2L
                coEvery { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = 2L) } returns firstPullList
                coEvery { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = 6L) } returns secondPullList
                coEvery { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = 9L) } returns emptyList()

                context.subject.doWork()

                coVerify(exactly = 1) {
                    context.accountEntityTagSyncTransaction.upsert(
                        accountId = context.accountId,
                        kind = case.kind,
                        entityTagList = firstPullList.map { pull -> pull.entityTag.toLocal() },
                        cursor = 6L,
                    )
                }
                coVerify(exactly = 1) {
                    context.accountEntityTagSyncTransaction.upsert(
                        accountId = context.accountId,
                        kind = case.kind,
                        entityTagList = secondPullList.map { pull -> pull.entityTag.toLocal() },
                        cursor = 9L,
                    )
                }
                coVerify(exactly = 3) { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = any()) }
            }

            test("TC-d6ad6c31-e1e9-435a-ab90-64a6c654d457 $label 내려받기가 지연되어도 나머지 종류의 내려받기를 먼저 시작한다") {
                runTest {
                    val context = case.context()
                    val callOrder = mutableListOf<String>()
                    coEvery { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = 0L) } coAnswers {
                        delay(PULL_DELAY)
                        callOrder += "entityTagPullEnd"
                        emptyList()
                    }
                    coEvery { context.tagRemoteDataSource.pull(usn = 0L) } coAnswers {
                        callOrder += "tagPullEnd"
                        emptyList()
                    }
                    coEvery { case.entityPull(this, context, 0L) } coAnswers {
                        callOrder += "entityPullEnd"
                        emptyList()
                    }
                    coEvery { context.memoRemoteDataSource.pull(usn = 0L) } coAnswers {
                        callOrder += "memoPullEnd"
                        emptyList()
                    }

                    context.subject.doWork()

                    callOrder.last() shouldBe "entityTagPullEnd"
                    callOrder.dropLast(1) shouldContainExactlyInAnyOrder listOf("tagPullEnd", "entityPullEnd", "memoPullEnd")
                }
            }

            test("TC-319f896c-9872-4455-b4d1-a91dc09e1e9b TC-51b016e3-825d-4ef5-9968-ecbf31b4c8a4 $label 내려받기가 실패해도 태그와 메모는 빈 응답까지 내려받고 동기화는 실패한다") {
                val context = case.context()
                val failure = TestException(Arb.string().next())
                val tagPullList = tagPulls(usnList = listOf(3L))
                val memoPullList = memoPulls(usnList = listOf(4L))
                coEvery { context.entityTagRemoteDataSource.pull(kind = case.remoteKind, usn = any()) } throws failure
                coEvery { context.tagRemoteDataSource.pull(usn = 0L) } returns tagPullList
                coEvery { context.tagRemoteDataSource.pull(usn = 3L) } returns emptyList()
                coEvery { context.memoRemoteDataSource.pull(usn = 0L) } returns memoPullList
                coEvery { context.memoRemoteDataSource.pull(usn = 4L) } returns emptyList()

                val actual =
                    shouldThrowExactly<TestException> {
                        context.subject.doWork()
                    }

                actual.message shouldBe failure.message
                coVerify(exactly = 1) {
                    context.accountTagSyncTransaction.upsert(
                        accountId = context.accountId,
                        tagList = tagPullList.map { pull -> pull.tag.toLocal() },
                        cursor = 3L,
                    )
                }
                coVerify(exactly = 1) {
                    context.accountMemoSyncTransaction.upsert(
                        accountId = context.accountId,
                        memoList = memoPullList.map { pull -> pull.memo.toLocal() },
                        cursor = 4L,
                    )
                }
                coVerify(exactly = 0) {
                    context.accountEntityTagSyncTransaction.upsert(accountId = any(), kind = case.kind, entityTagList = any(), cursor = any())
                }
            }

            test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 $label 만 업로드한다") {
                val accountId = Arb.kotlinUuid().next()
                val otherAccountId = Arb.kotlinUuid().next()
                val entityTagList = entityTags(size = 1)
                val context = case.context(accountId = accountId, entityTagList = entityTagList)
                coEvery {
                    context.entityTagSyncLocalDataSource.readPendingList(accountId = otherAccountId, kind = case.kind)
                } returns entityTags(size = 1)

                context.subject.doWork()

                coVerify(exactly = 1) {
                    context.entityTagRemoteDataSource.push(
                        kind = case.remoteKind,
                        entityTagList = entityTagList.map { entityTag -> entityTag.toRemote() },
                    )
                }
                coVerify(exactly = 1) { context.entityTagRemoteDataSource.push(any(), any()) }
                coVerify(exactly = 0) {
                    context.entityTagSyncLocalDataSource.readPendingList(accountId = otherAccountId, kind = any())
                }
            }
        }
    })
