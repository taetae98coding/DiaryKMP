package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
            test("TC-400fe05d-3f17-47cf-b0bb-ca12fe6625c0 곡 $itemCount 개를 최대 100개 단위로 중복과 누락 없이 요청한다") {
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

        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 삭제된 곡도 삭제 상태로 업로드된다") {
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

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 곡 업로드 묶음이 성공하면 보낸 항목의 대기 해제를 요청한다") {
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

        test("TC-929d599a-bd66-4c34-8b8f-fd0df8e6aeee 곡 업로드가 실패하면 내려받기를 진행하지 않는다") {
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

        test("TC-38d5d054-84c2-42b1-a9bf-75572a81a394 태그 업로드가 실패해도 곡은 끝까지 업로드한다") {
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

        test("TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 TC-21ff0303-9f3c-4cb3-9f35-4251ec918ee8 곡 내려받기는 기록된 순번부터 빈 응답까지 반복한다") {
            val context = context()
            val firstPullList = musicPulls(usnList = listOf(4L, 6L))
            val secondPullList = musicPulls(usnList = listOf(9L))
            coEvery { context.syncCursorLocalDataSource.read(accountId = context.accountId, kind = SyncKindLocalEntity.MUSIC) } returns 2L
            coEvery { context.musicRemoteDataSource.pull(usn = 2L) } returns firstPullList
            coEvery { context.musicRemoteDataSource.pull(usn = 6L) } returns secondPullList
            coEvery { context.musicRemoteDataSource.pull(usn = 9L) } returns emptyList()

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.upsert(
                    accountId = context.accountId,
                    musicList = firstPullList.map { pull -> pull.music.toLocal() },
                    cursor = 6L,
                )
            }
            coVerify(exactly = 1) {
                context.accountMusicSyncTransaction.upsert(
                    accountId = context.accountId,
                    musicList = secondPullList.map { pull -> pull.music.toLocal() },
                    cursor = 9L,
                )
            }
            coVerify(exactly = 3) { context.musicRemoteDataSource.pull(any()) }
        }

        test("TC-28dda1bd-78ef-4aef-943d-5bf25ae21551 실행 시점에 확인된 계정의 곡만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val context = context(accountId = accountId)
            coEvery {
                context.musicSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            } returns musics(size = 1)

            context.subject.doWork()

            coVerify(exactly = 1) {
                context.musicSyncLocalDataSource.readPendingList(accountId = accountId)
            }
            coVerify(exactly = 0) {
                context.musicSyncLocalDataSource.readPendingList(accountId = otherAccountId)
            }
            coVerify(exactly = 0) { context.musicRemoteDataSource.push(any()) }
        }
    }) {
    public companion object {
        private fun music(isDeleted: Boolean): MusicLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MusicLocalEntity>()
                .setExp(MusicLocalEntity::isDeleted, isDeleted)
                .setExp(MusicLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(MusicLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
