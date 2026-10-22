package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.coVerify
import kotlin.uuid.Uuid

class SyncWorkQrTest :
    FunSpec({
        test("TC-28a7ff40-be46-4362-a4de-ddf85ceba185 QR만 대기하면 QR 요청만 발생한다") {
            val context = context(qrList = qrs(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.qrRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 QR $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val qrList = qrs(size = itemCount)
                val context = context(qrList = qrList)
                val requests = mutableListOf<List<QrRemoteEntity>>()
                coEvery { context.qrRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<QrRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly qrList.map { qr -> qr.toRemote() }
            }
        }

        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 삭제된 QR도 삭제 상태로 업로드된다") {
            val qrList = listOf(fixtureMonkey.localQr(isDeleted = true), fixtureMonkey.localQr(isDeleted = false))
            val context = context(qrList = qrList)
            val requests = mutableListOf<List<QrRemoteEntity>>()
            coEvery { context.qrRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<QrRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly qrList.map { qr -> qr.toRemote() }
            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 QR 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val qrList = qrs(size = 101)
            val context = context(qrList = qrList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.clearPending(
                    accountId = context.accountId,
                    qrList = qrList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.clearPending(
                    accountId = context.accountId,
                    qrList = qrList.drop(100),
                )
            }
        }

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee TC-73f30279-c2bc-4cc8-afd7-41dad8268fc5 QR 업로드가 실패하면 업로드 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
            val context = context(qrList = qrs(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.qrRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.accountQrSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.qrRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그 업로드가 실패해도 QR은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    qrList = qrs(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 3) { context.qrRemoteDataSource.push(any()) }
        }

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 QR이 실패해도 태그와 장소와 웹 항목과 연락처와 곡은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 201),
                    placeList = places(size = 201),
                    webList = webs(size = 201),
                    contactList = contacts(size = 201),
                    musicList = musics(size = 201),
                    qrList = qrs(size = 1),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            val tagRequests = mutableListOf<Int>()
            val placeRequests = mutableListOf<Int>()
            val webRequests = mutableListOf<Int>()
            val contactRequests = mutableListOf<Int>()
            val musicRequests = mutableListOf<Int>()
            coEvery { context.qrRemoteDataSource.push(any()) } throws failure
            coEvery { context.tagRemoteDataSource.push(any()) } answers { tagRequests += firstArg<List<Any>>().size }
            coEvery { context.placeRemoteDataSource.push(any()) } answers { placeRequests += firstArg<List<Any>>().size }
            coEvery { context.webRemoteDataSource.push(any()) } answers { webRequests += firstArg<List<Any>>().size }
            coEvery { context.contactRemoteDataSource.push(any()) } answers { contactRequests += firstArg<List<Any>>().size }
            coEvery { context.musicRemoteDataSource.push(any()) } answers { musicRequests += firstArg<List<Any>>().size }

            shouldThrowExactly<TestException> { context.subject.doWork() }

            tagRequests shouldContainExactly listOf(100, 100, 1)
            placeRequests shouldContainExactly listOf(100, 100, 1)
            webRequests shouldContainExactly listOf(100, 100, 1)
            contactRequests shouldContainExactly listOf(100, 100, 1)
            musicRequests shouldContainExactly listOf(100, 100, 1)
        }

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 QR 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = qrPulls(usnList = listOf(4L, 6L))
            val secondPullList = qrPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.QR) } returns 2L
            coEvery { context.qrRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.qrRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.qrRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.upsert(
                    accountId = context.accountId,
                    qrList = firstPullList.map { pull -> pull.qr.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.upsert(
                    accountId = context.accountId,
                    qrList = secondPullList.map { pull -> pull.qr.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.qrRemoteDataSource.pull(any()) }
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 QR만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.qrSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            } returns qrs(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.qrSyncLocalDataSource.readPendingList(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.qrSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }
    })
