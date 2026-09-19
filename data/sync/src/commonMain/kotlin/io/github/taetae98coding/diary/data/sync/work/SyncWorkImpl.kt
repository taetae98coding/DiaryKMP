package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SyncWorkImpl(
    private val tagSyncWork: TagSyncWork,
    private val placeSyncWork: PlaceSyncWork,
    private val webSyncWork: WebSyncWork,
    private val contactSyncWork: ContactSyncWork,
    private val musicSyncWork: MusicSyncWork,
    private val memoSyncWork: MemoSyncWork,
    private val memoTagSyncWork: MemoTagSyncWork,
    private val memoPlaceSyncWork: MemoPlaceSyncWork,
    private val memoWebSyncWork: MemoWebSyncWork,
    private val tagLinkSyncWork: TagLinkSyncWork,
    private val webTagSyncWork: WebTagSyncWork,
    private val placeTagSyncWork: PlaceTagSyncWork,
) : SyncWork {
    override suspend fun doWork(accountId: Uuid) {
        try {
            push(accountId = accountId)
            pull(accountId = accountId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            DiaryLogger.log(log = CrashlyticsLog(message = "데이터 동기화 실패", throwable = throwable))
            throw throwable
        }
    }

    private suspend fun push(accountId: Uuid) {
        supervisorScope {
            val tag = async { tagSyncWork.push(accountId = accountId) }
            val place = async { placeSyncWork.push(accountId = accountId) }
            val web = async { webSyncWork.push(accountId = accountId) }
            val contact = async { contactSyncWork.push(accountId = accountId) }
            val music = async { musicSyncWork.push(accountId = accountId) }
            val memo =
                async {
                    tag.await()
                    memoSyncWork.push(accountId = accountId)
                }
            val memoTag =
                async {
                    tag.await()
                    memo.await()
                    memoTagSyncWork.push(accountId = accountId)
                }
            val memoPlace =
                async {
                    place.await()
                    memo.await()
                    memoPlaceSyncWork.push(accountId = accountId)
                }
            val memoWeb =
                async {
                    web.await()
                    memo.await()
                    memoWebSyncWork.push(accountId = accountId)
                }
            val tagLink =
                async {
                    tag.await()
                    tagLinkSyncWork.push(accountId = accountId)
                }
            val webTag =
                async {
                    tag.await()
                    web.await()
                    webTagSyncWork.push(accountId = accountId)
                }
            val placeTag =
                async {
                    tag.await()
                    place.await()
                    placeTagSyncWork.push(accountId = accountId)
                }

            listOf(tag, place, web, contact, music, memo, memoTag, memoPlace, memoWeb, tagLink, webTag, placeTag).awaitAllCatching()
        }
    }

    private suspend fun pull(accountId: Uuid) {
        supervisorScope {
            listOf(
                async { tagSyncWork.pull(accountId = accountId) },
                async { placeSyncWork.pull(accountId = accountId) },
                async { webSyncWork.pull(accountId = accountId) },
                async { contactSyncWork.pull(accountId = accountId) },
                async { musicSyncWork.pull(accountId = accountId) },
                async { memoSyncWork.pull(accountId = accountId) },
                async { memoTagSyncWork.pull(accountId = accountId) },
                async { memoPlaceSyncWork.pull(accountId = accountId) },
                async { memoWebSyncWork.pull(accountId = accountId) },
                async { tagLinkSyncWork.pull(accountId = accountId) },
                async { webTagSyncWork.pull(accountId = accountId) },
                async { placeTagSyncWork.pull(accountId = accountId) },
            ).awaitAllCatching()
        }
    }

    // 한 종류가 실패해도 나머지가 끝까지 진행하도록 모두 기다린 뒤 첫 실패를 전달한다.
    private suspend fun List<Deferred<Unit>>.awaitAllCatching() {
        map { deferred -> deferred.awaitCatching() }
            .forEach { result -> result.getOrThrow() }
    }

    private suspend fun Deferred<Unit>.awaitCatching(): Result<Unit> =
        try {
            Result.success(await())
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
}
