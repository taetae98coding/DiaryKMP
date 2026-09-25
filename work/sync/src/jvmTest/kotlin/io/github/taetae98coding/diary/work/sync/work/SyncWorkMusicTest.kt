package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.coVerify
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SyncWorkMusicTest :
    FunSpec({
        test("곡만 대기하면 곡 요청만 발생한다") {
            val context = context(musicList = musics(size = 1))

            context.subject.doWork()

            coVerify(exactly = 1) { context.musicRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.webRemoteDataSource.push(any()) }
            coVerify(exactly = 0) { context.memoRemoteDataSource.push(any()) }
        }

        listOf(
            1 to listOf(1),
            100 to listOf(100),
            201 to listOf(100, 100, 1),
        ).forEach { (itemCount, expectedRequestSizes) ->
            test("TC-DATA-SYNC-DATA-005 곡 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
                val musicList = musics(size = itemCount)
                val context = context(musicList = musicList)
                val requests = mutableListOf<List<MusicRemoteEntity>>()
                coEvery { context.musicRemoteDataSource.push(any()) } coAnswers {
                    requests += firstArg<List<MusicRemoteEntity>>()
                }

                context.subject.doWork()

                requests.map { request -> request.size } shouldContainExactly expectedRequestSizes
                requests.flatten() shouldContainExactly musicList.map { music -> music.toRemote() }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-007 삭제된 곡도 삭제 상태로 업로드된다") {
            val musicList = listOf(music(isDeleted = true), music(isDeleted = false))
            val context = context(musicList = musicList)
            val requests = mutableListOf<List<MusicRemoteEntity>>()
            coEvery { context.musicRemoteDataSource.push(any()) } coAnswers {
                requests += firstArg<List<MusicRemoteEntity>>()
            }

            context.subject.doWork()

            requests.flatten() shouldContainExactly musicList.map { music -> music.toRemote() }
            requests.flatten().map { request -> request.isDeleted } shouldContainExactly listOf(true, false)
        }

        test("TC-DATA-SYNC-DOMAIN-026 곡 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
            val musicList = musics(size = 101)
            val context = context(musicList = musicList)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.clearPending(
                    accountId = context.accountId,
                    musicList = musicList.take(100),
                )
            }
            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.clearPending(
                    accountId = context.accountId,
                    musicList = musicList.drop(100),
                )
            }
        }

        test("TC-DATA-SYNC-DOMAIN-029 곡 업로드가 실패하면 내려받기를 진행하지 않는다") {
            val context = context(musicList = musics(size = 1))
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.musicRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 0) { context.accountMusicSyncTransaction.clearPending(any(), any()) }
            coVerify(exactly = 0) { context.musicRemoteDataSource.pull(any()) }
            coVerify(exactly = 0) { context.tagRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-054 태그 업로드가 실패해도 곡은 끝까지 업로드한다") {
            val context =
                context(
                    tagList = tags(size = 1),
                    musicList = musics(size = 201),
                )
            val failure = TestException(fixtureMonkey.giveMeOne())
            coEvery { context.tagRemoteDataSource.push(any()) } throws failure

            shouldThrowExactly<TestException> {
                context.subject.doWork()
            }

            coVerify(exactly = 3) { context.musicRemoteDataSource.push(any()) }
        }

        test("TC-DATA-SYNC-DATA-017 TC-DATA-SYNC-DATA-020 곡 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = musicPulls(usnList = listOf(4L, 6L))
            val secondPullList = musicPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.find(accountId = context.accountId, kind = SyncKind.MUSIC) } returns 2L
            coEvery { context.musicRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.musicRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.musicRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.save(
                    accountId = context.accountId,
                    musicList = firstPullList.map { pull -> pull.music.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.save(
                    accountId = context.accountId,
                    musicList = secondPullList.map { pull -> pull.music.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.musicRemoteDataSource.pull(any()) }
        }

        test("TC-DATA-SYNC-DOMAIN-024 실행 시점에 확인된 계정의 곡만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.musicSyncLocalDataSource.findPending(accountId = otherAccountId)
            } returns musics(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.musicSyncLocalDataSource.findPending(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.musicSyncLocalDataSource.findPending(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.musicRemoteDataSource.push(any()) }
        }
    }) {
    public companion object {
        private fun music(isDeleted: Boolean): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::isDeleted, isDeleted)
                .setExp(MusicLocalEntity::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(MusicLocalEntity::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
