package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineCycleRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutinePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.localRoutine
import io.github.taetae98coding.diary.core.testing.routine.localRoutineRecord
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import kotlin.uuid.Uuid

class SyncWorkRoutineTest :
    FunSpec({
        test("TC-28a7ff40-be46-4362-a4de-ddf85ceba185 루틴만 대기하면 루틴 요청만 발생한다") {
            val context = context(routineList = routines(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.routineRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.routineRecordRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }

        test("TC-28a7ff40-be46-4362-a4de-ddf85ceba185 루틴 기록만 대기하면 루틴 기록 요청만 발생한다") {
            val context = context(routineRecordList = routineRecords(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.routineRecordRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.routineRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 루틴 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val routineList = routines(size = itemCount)
                val context = context(routineList = routineList)
                val requests = mutableListOf<List<RoutineRemoteEntity>>()
                coEvery { context.routineRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<RoutineRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly routineList.map { routine -> routine.toRemote() }
            }

            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 루틴 기록 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val recordList = routineRecords(size = itemCount)
                val context = context(routineRecordList = recordList)
                val requests = mutableListOf<List<RoutineRecordRemoteEntity>>()
                coEvery { context.routineRecordRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<RoutineRecordRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly recordList.map { record -> record.toRemote() }
            }
        }

        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 삭제된 루틴와 뺀 루틴 기록도 삭제 상태로 업로드된다") {
            val routineList = listOf(Arb.localRoutine(isDeleted = true).next(), Arb.localRoutine(isDeleted = false).next())
            val recordList =
                listOf(
                    Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = true).next(),
                    Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), isDeleted = false).next(),
                )
            val context = context(routineList = routineList, routineRecordList = recordList)
            val routineRequests = mutableListOf<List<RoutineRemoteEntity>>()
            val recordRequests = mutableListOf<List<RoutineRecordRemoteEntity>>()
            coEvery { context.routineRemoteDataSource.push(any()) } coAnswers {
                routineRequests += firstArg<List<RoutineRemoteEntity>>()
            }
            coEvery { context.routineRecordRemoteDataSource.push(any()) } coAnswers {
                recordRequests += firstArg<List<RoutineRecordRemoteEntity>>()
            }

            context.subject.doWork()

            routineRequests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
            recordRequests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-bfcaced9-f529-491c-a839-d267b96601aa 같은 루틴의 서로 다른 루틴 기록은 루틴 없이 각자의 전체 상태로 업로드된다") {
            val routineId = Arb.kotlinUuid().next()
            val recordList =
                listOf(
                    Arb.localRoutineRecord(routineId = routineId).next(),
                    Arb.localRoutineRecord(routineId = routineId).next(),
                )
            val context = context(routineRecordList = recordList)
            val requests = mutableListOf<List<RoutineRecordRemoteEntity>>()
            coEvery { context.routineRecordRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<RoutineRecordRemoteEntity>>()
            }

            context.subject.doWork()

            coVerify(exactly = 0) { context.routineRemoteDataSource.push(any()) }
            requests.flatten() shouldContainExactly recordList.map { record -> record.toRemote() }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드 묶음이 성공하면 보낸 루틴와 루틴 기록의 대기 해제를 요청한다") {
            val routineList = routines(size = 101)
            val recordList = routineRecords(size = 3)
            val context = context(routineList = routineList, routineRecordList = recordList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountRoutineSyncTransaction.clearPending(accountId = context.accountId, routineList = routineList.take(100))
            }
            coVerify(exactly = 1) {
                context.accountRoutineSyncTransaction.clearPending(accountId = context.accountId, routineList = routineList.drop(100))
            }
            coVerify(exactly = 1) {
                context.accountRoutineRecordSyncTransaction.clearPending(accountId = context.accountId, recordList = recordList)
            }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee 루틴 기록 업로드가 실패하면 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
            val context = context(routineRecordList = routineRecords(size = 1))
            val failure = TestException(Arb.string().next())
            coEvery { context.routineRecordRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 0) { context.accountRoutineRecordSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.routineRecordRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.routineRemoteDataSource.pull(any()) }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee 루틴 업로드가 실패하면 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
            val context = context(routineList = routines(size = 1))
            val failure = TestException(Arb.string().next())
            coEvery { context.routineRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 0) { context.accountRoutineSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.routineRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-f2279afc-4baf-4ab0-913a-1c7e368bfebb 루틴 업로드가 성공하고 루틴 기록 업로드가 실패하면 루틴의 대기 해제만 유지된다") {
            val routine = Arb.localRoutine(isDeleted = false).next()
            val record = Arb.localRoutineRecord(routineId = routine.id).next()
            val context = context(routineList = listOf(routine), routineRecordList = listOf(record))
            val failure = TestException(Arb.string().next())
            coEvery { context.routineRecordRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 1) { context.accountRoutineSyncTransaction.clearPending(accountId = context.accountId, routineList = listOf(routine)) }
            coVerify(exactly = 0) { context.accountRoutineRecordSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.routineRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.routineRecordRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그 업로드가 실패해도 루틴와 루틴 기록은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    routineList = routines(size = 201),
                    routineRecordList = routineRecords(size = 201),
                )
            val failure = TestException(Arb.string().next())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 3) { context.routineRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.routineRecordRemoteDataSource.push(any()) }
        }

        listOf(
            "루틴" to true,
            "루틴 기록" to false,
        ).forEach { (failingLabel, isRoutineFailing) ->
            test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 $failingLabel 이 실패해도 태그와 장소와 웹 항목과 연락처와 곡과 QR은 끝까지 업로드한다") {
                val context =
                    context(
                        tagList = tags(size = 201),
                        placeList = places(size = 201),
                        webList = webs(size = 201),
                        contactList = contacts(size = 201),
                        musicList = musics(size = 201),
                        qrList = qrs(size = 201),
                        routineList = routines(size = 1),
                        routineRecordList = routineRecords(size = 1),
                    )
                val failure = TestException(Arb.string().next())
                val requestMap = mutableMapOf<String, MutableList<Int>>()
                if (isRoutineFailing) {
                    coEvery { context.routineRemoteDataSource.push(any()) } throws failure
                } else {
                    coEvery { context.routineRecordRemoteDataSource.push(any()) } throws failure
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

        test("TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 루틴와 루틴 기록 내려받기는 각자 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val routinePullList = routinePulls(usnList = listOf(4L))
            val recordPullList = routineRecordPulls(usnList = listOf(7L, 8L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.ROUTINE) } returns 2L
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.ROUTINE_RECORD) } returns 5L
            coEvery { context.routineRemoteDataSource.pull(usn = 2L) } returns routinePullList
            coEvery { context.routineRemoteDataSource.pull(usn = 4L) } returns emptyList()
            coEvery { context.routineRecordRemoteDataSource.pull(usn = 5L) } returns recordPullList
            coEvery { context.routineRecordRemoteDataSource.pull(usn = 8L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountRoutineSyncTransaction.upsert(
                    accountId = context.accountId,
                    routineList = routinePullList.map { pull -> pull.routine.toLocal() },
                    cursor = 4L,
                )
            }
            coVerify(exactly = 1) {
                context.accountRoutineRecordSyncTransaction.upsert(
                    accountId = context.accountId,
                    recordList = recordPullList.map { pull -> pull.routineRecord.toLocal() },
                    cursor = 8L,
                )
            }
            coVerify(exactly = 2) { context.routineRemoteDataSource.pull(any()) }
            coVerify(exactly = 2) { context.routineRecordRemoteDataSource.pull(any()) }
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 루틴와 루틴 기록만 업로드한다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val routineList = routines(size = 1)
            val recordList = routineRecords(size = 1)
            val context = context(accountId = accountId, routineList = routineList, routineRecordList = recordList)
            coEvery { context.routineSyncLocalDataSource.readPendingList(accountId = otherAccountId) } returns routines(size = 1)
            coEvery { context.routineRecordSyncLocalDataSource.readPendingList(accountId = otherAccountId) } returns routineRecords(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.routineRemoteDataSource.push(routineList.map { routine -> routine.toRemote() })
            }
            coVerify(exactly = 1) { context.routineRecordRemoteDataSource.push(recordList.map { record -> record.toRemote() }) }
            coVerify(exactly = 1) { context.routineRemoteDataSource.push(any()) }
            coVerify(exactly = 1) { context.routineRecordRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.routineSyncLocalDataSource.readPendingList(accountId = otherAccountId) }
            coVerify(exactly = 0) { context.routineRecordSyncLocalDataSource.readPendingList(accountId = otherAccountId) }
        }

        listOf(
            2000 to false,
            null to true,
        ).forEach { (goalAmount, isFinished) ->
            test("TC-02485cfa-c629-468a-a468-06d1efb26fab 목표량 ${goalAmount ?: "없음"}, 완료 여부 ${isFinished}인 루틴 업로드에는 전체 상태와 연·월·일 날짜가 담긴다") {
                withTimeZone("Asia/Seoul") {
                    val stored = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), goalAmount = goalAmount, isFinished = isFinished, isDeleted = Arb.boolean().next()).next()
                    val context = context(routineList = listOf(stored))
                    val requests = mutableListOf<List<RoutineRemoteEntity>>()
                    coEvery { context.routineRemoteDataSource.push(any()) } answers { requests += firstArg<List<RoutineRemoteEntity>>() }

                    context.subject.doWork()

                    val pushed = requests.single().single()
                    pushed shouldBe stored.toRemote()
                    pushed.detail.title shouldBe stored.detail.title
                    pushed.detail.description shouldBe stored.detail.description
                    pushed.detail.color shouldBe stored.detail.color
                    pushed.detail.unit shouldBe stored.detail.unit
                    pushed.detail.amountPerRecord shouldBe stored.detail.amountPerRecord
                    pushed.isDeleted shouldBe stored.isDeleted
                    pushed.detail.startDate shouldBe LocalDate(2026, 3, 1)
                    pushed.detail.goalAmount shouldBe goalAmount
                    pushed.isFinished shouldBe isFinished
                }
            }
        }

        listOf(
            RoutineCycleLocalEntity.DAILY to RoutineCycleRemoteEntity.DAILY,
            RoutineCycleLocalEntity.WEEKDAYS to RoutineCycleRemoteEntity.WEEKDAYS,
            RoutineCycleLocalEntity.WEEKENDS to RoutineCycleRemoteEntity.WEEKENDS,
            RoutineCycleLocalEntity.WEEKLY to RoutineCycleRemoteEntity.WEEKLY,
            RoutineCycleLocalEntity.MONTHLY to RoutineCycleRemoteEntity.MONTHLY,
        ).forEach { (cycle, remoteCycle) ->
            test("TC-483afe78-01ae-44e6-b1e9-8deca12073b7 $cycle 루틴 업로드에는 같은 뜻의 주기가 담긴다") {
                val context = context(routineList = listOf(Arb.localRoutine(startDate = LocalDate(2026, 3, 1), cycle = cycle).next()))
                val requests = mutableListOf<List<RoutineRemoteEntity>>()
                coEvery { context.routineRemoteDataSource.push(any()) } answers { requests += firstArg<List<RoutineRemoteEntity>>() }

                context.subject.doWork()

                requests
                    .single()
                    .single()
                    .detail.cycle shouldBe remoteCycle
            }

            test("TC-d2c02683-b789-4a75-badf-29489a2c1cde 내려받은 $remoteCycle 주기는 그 값이 뜻하는 주기로 저장된다") {
                val response = routinePulls(usnList = listOf(1L)).single().let { pull -> pull.copy(routine = pull.routine.copy(detail = pull.routine.detail.copy(cycle = remoteCycle))) }

                pulledRoutine(response = response.routine).detail.cycle shouldBe cycle
            }
        }

        listOf("회", "ml", "ML", "분 걷기", "kilometers", "").forEachIndexed { index, unit ->
            test("TC-06a7ffb0-3d16-48fd-a03f-423fae4bc4e3 $index: 루틴 업로드에는 단위가 적은 글자 그대로 담긴다") {
                val context = context(routineList = listOf(Arb.localRoutine(startDate = LocalDate(2026, 3, 1), unit = unit).next()))
                val requests = mutableListOf<List<RoutineRemoteEntity>>()
                coEvery { context.routineRemoteDataSource.push(any()) } answers { requests += firstArg<List<RoutineRemoteEntity>>() }

                context.subject.doWork()

                requests
                    .single()
                    .single()
                    .detail.unit shouldBe unit
            }
        }

        listOf("회", "km", "ML", "분 걷기", "kilometers", "").forEachIndexed { index, unit ->
            test("TC-4534eddf-89a0-4740-a48a-fd3accd33b78 $index: 내려받은 루틴의 단위는 글자 그대로 저장하고 내려받기 위치를 응답의 순번으로 옮긴다") {
                val context = context()
                val pull = routinePulls(usnList = listOf(11L)).single().let { pull -> pull.copy(routine = pull.routine.copy(detail = pull.routine.detail.copy(unit = unit))) }
                coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.ROUTINE) } returns 10L
                coEvery { context.routineRemoteDataSource.pull(usn = 10L) } returns listOf(pull)
                coEvery { context.routineRemoteDataSource.pull(usn = 11L) } returns emptyList()

                context.subject.doWork()

                coVerify(exactly = 1) { context.accountRoutineSyncTransaction.upsert(accountId = context.accountId, routineList = listOf(pull.routine.toLocal()), cursor = 11L) }
                pull.routine
                    .toLocal()
                    .detail.unit shouldBe unit
            }
        }

        test("TC-45c868d3-1f7f-44d3-9688-0d9d57ea964e 목표량이 비어 있는 루틴을 내려받으면 기기에 저장하는 루틴은 목표량이 없다") {
            val stored = Arb.localRoutine(startDate = LocalDate(2026, 3, 1), goalAmount = 2000).next()
            val response = stored.toRemote().let { remote -> remote.copy(detail = remote.detail.copy(goalAmount = null)) }

            val saved = pulledRoutine(response = response)

            saved.detail.goalAmount.shouldBeNull()
        }

        listOf(true, false).forEach { isFinished ->
            test("내려받은 루틴의 완료 여부 ${isFinished}는 그대로 저장된다") {
                val response =
                    Arb
                        .localRoutine(startDate = LocalDate(2026, 3, 1), isFinished = !isFinished)
                        .next()
                        .toRemote()
                        .copy(isFinished = isFinished)

                pulledRoutine(response = response).isFinished shouldBe isFinished
            }
        }

        test("TC-89458657-5b6f-4db1-8dbf-f5dd41305c9f 루틴 기록 업로드의 날짜는 연·월·일만 담긴다") {
            withTimeZone("Asia/Seoul") {
                val stored = Arb.localRoutineRecord(routineId = Arb.kotlinUuid().next(), date = LocalDate(2026, 3, 1), amount = 300).next()
                val context = context(routineRecordList = listOf(stored))
                val requests = mutableListOf<List<RoutineRecordRemoteEntity>>()
                coEvery { context.routineRecordRemoteDataSource.push(any()) } answers { requests += firstArg<List<RoutineRecordRemoteEntity>>() }

                context.subject.doWork()

                val pushed = requests.single().single()
                pushed shouldBe stored.toRemote()
                pushed.date shouldBe LocalDate(2026, 3, 1)
                pushed.amount shouldBe 300
            }
        }

        listOf("UTC", "Asia/Seoul", "Pacific/Honolulu").forEach { timeZoneId ->
            test("TC-d9851777-4709-4023-9a95-513efbdf274b $timeZoneId 시간대에서도 내려받은 루틴과 루틴 기록의 날짜가 같은 연·월·일로 저장된다") {
                withTimeZone(timeZoneId) {
                    val routineResponse = Arb.localRoutine(startDate = LocalDate(2026, 3, 1)).next().toRemote()
                    val recordResponse = Arb.localRoutineRecord(routineId = routineResponse.id, date = LocalDate(2026, 3, 1)).next().toRemote()

                    val savedRoutine = pulledRoutine(response = routineResponse)
                    val savedRecord = pulledRoutineRecord(response = recordResponse)

                    savedRoutine.detail.startDate shouldBe LocalDate(2026, 3, 1)
                    savedRecord.date shouldBe LocalDate(2026, 3, 1)
                    savedRecord.routineId shouldBe routineResponse.id
                }
            }
        }

        test("TC-c7c8b058-c682-4007-9209-94574e097147 내려받은 루틴 묶음을 읽지 못하면 그 묶음과 내려받기 위치를 반영하지 않고 다음 계기에 같은 순번부터 다시 요청한다") {
            val context = context()
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.ROUTINE) } returns 10L
            coEvery { context.routineRemoteDataSource.pull(usn = 10L) } throws TestException(Arb.string().next())

            shouldThrowExactly<TestException> { context.subject.doWork() }
            shouldThrowExactly<TestException> { context.subject.doWork() }

            coVerify(exactly = 0) { context.accountRoutineSyncTransaction.upsert(any(), any(), any()) }
            coVerify(exactly = 2) { context.routineRemoteDataSource.pull(usn = 10L) }
            coVerify(exactly = 2) { context.routineRemoteDataSource.pull(any()) }
        }

        test("TC-159f47b4-d880-4ef7-bf64-b0f4a7a4465d 루틴 묶음이 실패하면 루틴 기록과 루틴·태그 연결을 시도하지 않고 태그와 QR은 모두 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 201),
                    qrList = qrs(size = 201),
                    routineList = routines(size = 201),
                    routineRecordList = routineRecords(size = 1),
                    entityTagMap = mapOf(EntityTagKindLocalEntity.ROUTINE to entityTags(size = 1)),
                )
            val failure = TestException(Arb.string().next())
            val routineRequestCount = mutableListOf<Int>()
            coEvery { context.routineRemoteDataSource.push(any()) } coAnswers {
                routineRequestCount += 1
                if (routineRequestCount.size == 2) throw failure
            }

            shouldThrowExactly<TestException> { context.subject.doWork() }

            routineRequestCount.size shouldBe 2
            coVerify(exactly = 0) { context.routineRecordRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.entityTagRemoteDataSource.push(kind = EntityTagKindRemoteEntity.ROUTINE, entityTagList = any()) }
            coVerify(exactly = 3) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 3) { context.qrRemoteDataSource.push(any()) }
        }
    })

private suspend fun pulledRoutine(response: RoutineRemoteEntity): RoutineLocalEntity {
    val context = context()
    coEvery { context.routineRemoteDataSource.pull(usn = 0L) } returns listOf(RoutinePullRemoteEntity(routine = response, usn = 1L))
    coEvery { context.routineRemoteDataSource.pull(usn = 1L) } returns emptyList()
    val savedList = mutableListOf<List<RoutineLocalEntity>>()
    coEvery { context.accountRoutineSyncTransaction.upsert(any(), any(), any()) } answers { savedList += secondArg<List<RoutineLocalEntity>>() }

    context.subject.doWork()

    return savedList.single().single()
}

private suspend fun pulledRoutineRecord(response: RoutineRecordRemoteEntity): RoutineRecordLocalEntity {
    val context = context()
    coEvery { context.routineRecordRemoteDataSource.pull(usn = 0L) } returns listOf(RoutineRecordPullRemoteEntity(routineRecord = response, usn = 1L))
    coEvery { context.routineRecordRemoteDataSource.pull(usn = 1L) } returns emptyList()
    val savedList = mutableListOf<List<RoutineRecordLocalEntity>>()
    coEvery { context.accountRoutineRecordSyncTransaction.upsert(any(), any(), any()) } answers { savedList += secondArg<List<RoutineRecordLocalEntity>>() }

    context.subject.doWork()

    return savedList.single().single()
}

private suspend fun withTimeZone(
    timeZoneId: String,
    block: suspend () -> Unit,
) {
    val defaultTimeZone = TimeZone.getDefault()
    try {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
        block()
    } finally {
        TimeZone.setDefault(defaultTimeZone)
    }
}
