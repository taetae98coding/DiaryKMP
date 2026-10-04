package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.contact.datasource.ContactRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class ContactSyncWork(
    private val accountContactSyncLocalDataSource: AccountContactSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountContactSyncTransaction: AccountContactSyncTransaction,
    private val contactRemoteDataSource: ContactRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountContactSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> contactRemoteDataSource.push(contactList = chunk.map { contact -> contact.toRemote() }) },
            clearPending = { chunk -> accountContactSyncTransaction.clearPending(accountId = accountId, contactList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.CONTACT,
            pull = { cursor -> contactRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountContactSyncTransaction.upsert(
                    accountId = accountId,
                    contactList = pullList.map { pull -> pull.contact.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
