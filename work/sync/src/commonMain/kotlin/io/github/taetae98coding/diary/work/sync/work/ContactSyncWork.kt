package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
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
        val contactList = accountContactSyncLocalDataSource.findPending(accountId = accountId)

        contactList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            contactRemoteDataSource.push(contactList = chunk.map { contact -> contact.toRemote() })
            accountContactSyncTransaction.clearPending(accountId = accountId, contactList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.CONTACT,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = contactRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountContactSyncTransaction.save(
                    accountId = accountId,
                    contactList = pullList.map { pull -> pull.contact.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
