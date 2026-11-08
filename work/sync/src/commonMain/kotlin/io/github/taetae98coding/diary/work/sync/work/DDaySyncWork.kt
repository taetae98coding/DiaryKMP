package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountDDaySyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.dday.transaction.AccountDDaySyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.dday.datasource.DDayRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class DDaySyncWork(
    private val accountDDaySyncLocalDataSource: AccountDDaySyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountDDaySyncTransaction: AccountDDaySyncTransaction,
    private val ddayRemoteDataSource: DDayRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountDDaySyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> ddayRemoteDataSource.push(ddayList = chunk.map { dday -> dday.toRemote() }) },
            clearPending = { chunk -> accountDDaySyncTransaction.clearPending(accountId = accountId, ddayList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.DDAY,
            pull = { cursor -> ddayRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountDDaySyncTransaction.upsert(
                    accountId = accountId,
                    ddayList = pullList.map { pull -> pull.dday.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
