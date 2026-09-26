package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
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
        test("TC-DATA-SYNC-DOMAIN-020 QR만 대기하면 QR 요청만 발생한다") {
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
            test("TC-DATA-SYNC-DATA-005 QR $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
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

        test("TC-DATA-SYNC-DOMAIN-007 삭제된 QR도 삭제 상태로 업로드된다") {
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

        test("TC-DATA-SYNC-DOMAIN-026 QR 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
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

        test("TC-DATA-SYNC-DOMAIN-029 TC-QR-ADD-DATA-004 QR 업로드가 실패하면 업로드 대기를 해제하지 않고 내려받기를 진행하지 않는다") {
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

        test("TC-DATA-SYNC-DOMAIN-054 태그 업로드가 실패해도 QR은 끝까지 업로드한다") {
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

        test("TC-DATA-SYNC-DOMAIN-054 QR이 실패해도 태그와 장소와 웹 항목과 연락처와 곡은 끝까지 업로드한다") {
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

        test("TC-DATA-SYNC-DATA-017 TC-DATA-SYNC-DATA-020 QR 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = qrPulls(usnList = listOf(4L, 6L))
            val secondPullList = qrPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.QR) } returns 2L
            coEvery { context.qrRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.qrRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.qrRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.save(
                    accountId = context.accountId,
                    qrList = firstPullList.map { pull -> pull.qr.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountQrSyncTransaction.save(
                    accountId = context.accountId,
                    qrList = secondPullList.map { pull -> pull.qr.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.qrRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-024 실행 시점에 확인된 계정의 QR만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.qrSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns qrs(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.qrSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.qrSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.qrRemoteDataSource.push(any()) }
        }
    })
