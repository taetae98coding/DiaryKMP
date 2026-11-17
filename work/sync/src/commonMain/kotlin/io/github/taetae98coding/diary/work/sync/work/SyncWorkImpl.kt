package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.sync.usecase.PrepareSyncUseCase
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
    private val supabaseAuth: SupabaseAuth,
    private val prepareSyncUseCase: PrepareSyncUseCase,
    tagSyncWork: TagSyncWork,
    placeSyncWork: PlaceSyncWork,
    webSyncWork: WebSyncWork,
    contactSyncWork: ContactSyncWork,
    musicSyncWork: MusicSyncWork,
    qrSyncWork: QrSyncWork,
    checklistSyncWork: ChecklistSyncWork,
    checklistItemSyncWork: ChecklistItemSyncWork,
    ddaySyncWork: DDaySyncWork,
    routineSyncWork: RoutineSyncWork,
    routineRecordSyncWork: RoutineRecordSyncWork,
    memoSyncWork: MemoSyncWork,
    memoTagSyncWork: MemoTagSyncWork,
    memoPlaceSyncWork: MemoPlaceSyncWork,
    memoWebSyncWork: MemoWebSyncWork,
    memoContactSyncWork: MemoContactSyncWork,
    tagLinkSyncWork: TagLinkSyncWork,
    entityTagSyncWork: EntityTagSyncWork,
    private val accountSyncTimeLocalDataSource: AccountSyncTimeLocalDataSource,
    private val clock: Clock,
) : SyncWork {
    // 먼저 끝나야 하는 종류로는 앞에서 선언한 단계만 넘길 수 있다. 선언 순서대로 시작하고, 여러 종류가 실패하면 앞선 종류의 실패를 전달한다.
    private val stepList: List<SyncStep> =
        buildList<SyncStep> {
            val tag =
                step(
                    push = { accountId -> tagSyncWork.push(accountId = accountId) },
                    pull = { accountId -> tagSyncWork.pull(accountId = accountId) },
                )
            val place =
                step(
                    push = { accountId -> placeSyncWork.push(accountId = accountId) },
                    pull = { accountId -> placeSyncWork.pull(accountId = accountId) },
                )
            val web =
                step(
                    push = { accountId -> webSyncWork.push(accountId = accountId) },
                    pull = { accountId -> webSyncWork.pull(accountId = accountId) },
                )
            val contact =
                step(
                    push = { accountId -> contactSyncWork.push(accountId = accountId) },
                    pull = { accountId -> contactSyncWork.pull(accountId = accountId) },
                )
            val music =
                step(
                    push = { accountId -> musicSyncWork.push(accountId = accountId) },
                    pull = { accountId -> musicSyncWork.pull(accountId = accountId) },
                )
            val qr =
                step(
                    push = { accountId -> qrSyncWork.push(accountId = accountId) },
                    pull = { accountId -> qrSyncWork.pull(accountId = accountId) },
                )
            val checklist =
                step(
                    push = { accountId -> checklistSyncWork.push(accountId = accountId) },
                    pull = { accountId -> checklistSyncWork.pull(accountId = accountId) },
                )
            step(
                prerequisiteList = listOf(checklist),
                push = { accountId -> checklistItemSyncWork.push(accountId = accountId) },
                pull = { accountId -> checklistItemSyncWork.pull(accountId = accountId) },
            )
            val dday =
                step(
                    push = { accountId -> ddaySyncWork.push(accountId = accountId) },
                    pull = { accountId -> ddaySyncWork.pull(accountId = accountId) },
                )
            val routine =
                step(
                    push = { accountId -> routineSyncWork.push(accountId = accountId) },
                    pull = { accountId -> routineSyncWork.pull(accountId = accountId) },
                )
            step(
                prerequisiteList = listOf(routine),
                push = { accountId -> routineRecordSyncWork.push(accountId = accountId) },
                pull = { accountId -> routineRecordSyncWork.pull(accountId = accountId) },
            )
            val memo =
                step(
                    prerequisiteList = listOf(tag),
                    push = { accountId -> memoSyncWork.push(accountId = accountId) },
                    pull = { accountId -> memoSyncWork.pull(accountId = accountId) },
                )
            step(
                prerequisiteList = listOf(tag, memo),
                push = { accountId -> memoTagSyncWork.push(accountId = accountId) },
                pull = { accountId -> memoTagSyncWork.pull(accountId = accountId) },
            )
            step(
                prerequisiteList = listOf(place, memo),
                push = { accountId -> memoPlaceSyncWork.push(accountId = accountId) },
                pull = { accountId -> memoPlaceSyncWork.pull(accountId = accountId) },
            )
            step(
                prerequisiteList = listOf(web, memo),
                push = { accountId -> memoWebSyncWork.push(accountId = accountId) },
                pull = { accountId -> memoWebSyncWork.pull(accountId = accountId) },
            )
            step(
                prerequisiteList = listOf(contact, memo),
                push = { accountId -> memoContactSyncWork.push(accountId = accountId) },
                pull = { accountId -> memoContactSyncWork.pull(accountId = accountId) },
            )
            step(
                prerequisiteList = listOf(tag),
                push = { accountId -> tagLinkSyncWork.push(accountId = accountId) },
                pull = { accountId -> tagLinkSyncWork.pull(accountId = accountId) },
            )

            EntityTagKindLocalEntity.entries.forEach { kind ->
                val entity =
                    when (kind) {
                        EntityTagKindLocalEntity.WEB -> web
                        EntityTagKindLocalEntity.PLACE -> place
                        EntityTagKindLocalEntity.CONTACT -> contact
                        EntityTagKindLocalEntity.CHECKLIST -> checklist
                        EntityTagKindLocalEntity.MUSIC -> music
                        EntityTagKindLocalEntity.QR -> qr
                        EntityTagKindLocalEntity.DDAY -> dday
                        EntityTagKindLocalEntity.ROUTINE -> routine
                    }

                step(
                    prerequisiteList = listOf(tag, entity),
                    push = { accountId -> entityTagSyncWork.push(accountId = accountId, kind = kind) },
                    pull = { accountId -> entityTagSyncWork.pull(accountId = accountId, kind = kind) },
                )
            }
        }

    override suspend fun doWork() {
        val account =
            when (val confirmedAccount = awaitConfirmedAccount()) {
                is Account.Guest -> return
                is Account.User -> confirmedAccount
            }
        val accountId = account.id

        // 플랫폼마다 다른 주기 예약기가 요청 경로를 거치지 않고 이 작업을 바로 실행하므로 준비를 요청 시점이 아니라 여기서 한다.
        prepareSyncUseCase(parameter = account).getOrThrow()
        push(accountId = accountId)
        pull(accountId = accountId)
        accountSyncTimeLocalDataSource.upsert(accountId = accountId, syncedAt = clock.now())
    }

    private suspend fun awaitConfirmedAccount(): Account {
        refreshStoppedSession()

        return getAccountUseCase()
            .map { result -> result.getOrThrow() }
            .first { account ->
                when (account) {
                    is Account.Guest -> true
                    is Account.User -> account.isSessionValid
                }
            }
    }

    // 갱신에 실패해도 다시 시도하지 않고, 세션이 갱신될 때까지 기다리는 기존 판단에 맡긴다.
    private suspend fun refreshStoppedSession() {
        try {
            supabaseAuth.refreshStoppedSession()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
        }
    }

    private suspend fun push(accountId: Uuid) {
        supervisorScope {
            val pushMap = mutableMapOf<SyncStep, Deferred<Unit>>()

            stepList.forEach { step ->
                val prerequisiteList = step.prerequisiteList.map { prerequisite -> pushMap.getValue(prerequisite) }

                pushMap[step] =
                    async {
                        prerequisiteList.forEach { prerequisite -> prerequisite.await() }
                        step.push(accountId)
                    }
            }

            pushMap.values.toList().awaitAllCatching()
        }
    }

    private suspend fun pull(accountId: Uuid) {
        supervisorScope {
            stepList
                .map { step -> async { step.pull(accountId) } }
                .awaitAllCatching()
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
