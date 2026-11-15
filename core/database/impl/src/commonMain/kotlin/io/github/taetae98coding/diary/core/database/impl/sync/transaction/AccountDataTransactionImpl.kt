package io.github.taetae98coding.diary.core.database.impl.sync.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.sync.transaction.AccountDataTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.dao.AccountDataDao
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDataTransactionImpl(
    private val database: DiaryDatabase,
) : AccountDataTransaction {
    override suspend fun delete(accountId: Uuid) {
        database.withWriteTransaction {
            val dao = database.accountDataDao()

            deleteLink(dao = dao, accountId = accountId)
            deleteEntity(dao = dao, accountId = accountId)

            dao.deleteTagFilter(accountId = accountId)
            dao.deleteMemoFilterTag(accountId = accountId)
            dao.deleteCalendarFilterTag(accountId = accountId)
            dao.deleteDDayFilterTag(accountId = accountId)
            dao.deleteRoutineFilterTag(accountId = accountId)
            dao.deleteRoutineFilter(accountId = accountId)
            dao.deleteSyncCursor(accountId = accountId)
        }
    }

    private suspend fun deleteLink(
        dao: AccountDataDao,
        accountId: Uuid,
    ) {
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

        dao.deleteContactTag(accountId = accountId)
        dao.deleteAccountContactTag(accountId = accountId)

        dao.deleteChecklistTag(accountId = accountId)
        dao.deleteAccountChecklistTag(accountId = accountId)

        dao.deleteMusicTag(accountId = accountId)
        dao.deleteAccountMusicTag(accountId = accountId)

        dao.deleteQrTag(accountId = accountId)
        dao.deleteAccountQrTag(accountId = accountId)

        dao.deleteDDayTag(accountId = accountId)
        dao.deleteAccountDDayTag(accountId = accountId)

        dao.deleteRoutineTag(accountId = accountId)
        dao.deleteAccountRoutineTag(accountId = accountId)
    }

    private suspend fun deleteEntity(
        dao: AccountDataDao,
        accountId: Uuid,
    ) {
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

        dao.deleteQr(accountId = accountId)
        dao.deleteAccountQr(accountId = accountId)

        dao.deleteChecklistItem(accountId = accountId)
        dao.deleteAccountChecklistItem(accountId = accountId)

        dao.deleteChecklist(accountId = accountId)
        dao.deleteAccountChecklist(accountId = accountId)

        dao.deleteDDay(accountId = accountId)
        dao.deleteAccountDDay(accountId = accountId)

        dao.deleteRoutineRecord(accountId = accountId)
        dao.deleteAccountRoutineRecord(accountId = accountId)

        dao.deleteRoutine(accountId = accountId)
        dao.deleteAccountRoutine(accountId = accountId)
    }
}
