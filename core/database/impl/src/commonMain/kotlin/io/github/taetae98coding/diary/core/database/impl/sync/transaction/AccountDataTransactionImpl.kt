package io.github.taetae98coding.diary.core.database.impl.sync.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.transaction.AccountDataTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDataTransactionImpl(
    private val database: DiaryDatabase,
) : AccountDataTransaction {
    override suspend fun delete(accountId: Uuid) {
        database.withWriteTransaction {
            val dao = database.accountDataDao()

            dao.deleteMemoTag(accountId = accountId)
            dao.deleteAccountMemoTag(accountId = accountId)

            dao.deleteMemoPlace(accountId = accountId)
            dao.deleteAccountMemoPlace(accountId = accountId)

            dao.deleteMemoWeb(accountId = accountId)
            dao.deleteAccountMemoWeb(accountId = accountId)

            dao.deleteMemoContact(accountId = accountId)
            dao.deleteAccountMemoContact(accountId = accountId)

            dao.deleteTagLink(accountId = accountId)
            dao.deleteAccountTagLink(accountId = accountId)

            dao.deleteWebTag(accountId = accountId)
            dao.deleteAccountWebTag(accountId = accountId)

            dao.deletePlaceTag(accountId = accountId)
            dao.deleteAccountPlaceTag(accountId = accountId)

            dao.deleteMemo(accountId = accountId)
            dao.deleteAccountMemo(accountId = accountId)

            dao.deleteTag(accountId = accountId)
            dao.deleteAccountTag(accountId = accountId)

            dao.deletePlace(accountId = accountId)
            dao.deleteAccountPlace(accountId = accountId)

            dao.deleteWeb(accountId = accountId)
            dao.deleteAccountWeb(accountId = accountId)

            dao.deleteContact(accountId = accountId)
            dao.deleteAccountContact(accountId = accountId)

            dao.deleteMusic(accountId = accountId)
            dao.deleteAccountMusic(accountId = accountId)

            dao.deleteTagFilter(accountId = accountId)
            dao.deleteMemoFilterTag(accountId = accountId)
            dao.deleteCalendarFilterTag(accountId = accountId)
            dao.deleteSyncCursor(accountId = accountId)
        }
    }
}
