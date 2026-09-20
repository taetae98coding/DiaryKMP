package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
internal class SyncWorkImpl(
    private val getAccountUseCase: GetAccountUseCase,
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
    private val accountSyncTimeLocalDataSource: AccountSyncTimeLocalDataSource,
    private val clock: Clock,
) : SyncWork {
    override suspend fun doWork() {
        try {
            val accountId =
                when (val account = awaitConfirmedAccount()) {
                    is Account.Guest -> return
                    is Account.User -> account.id
                }

            push(accountId = accountId)
            pull(accountId = accountId)
            accountSyncTimeLocalDataSource.upsert(accountId = accountId, syncedAt = clock.now())
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            DiaryLogger.log(log = CrashlyticsLog(message = "데이터 동기화 실패", throwable = throwable))
            throw throwable
        }
    }

    // 저장된 사용자 정보가 로그인 세션보다 먼저 확인되므로, 세션이 갱신되지 않은 사용자는 아직 확정되지 않은 것으로 보고 기다린다.
    // 기다리지 않으면 시스템이 앱을 깨워 실행한 작업이 매번 아무것도 동기화하지 못하고 끝난다.
    private suspend fun awaitConfirmedAccount(): Account =
        getAccountUseCase(parameter = Unit)
            .map { result -> result.getOrThrow() }
            .first { account ->
                when (account) {
                    is Account.Guest -> true
                    is Account.User -> account.isSessionValid
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
