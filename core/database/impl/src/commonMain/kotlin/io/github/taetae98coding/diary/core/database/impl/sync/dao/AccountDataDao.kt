package io.github.taetae98coding.diary.core.database.impl.sync.dao

import androidx.room3.Dao
import androidx.room3.Query
import kotlin.uuid.Uuid

@Dao
@Suppress("ComplexInterface", "TooManyFunctions")
internal interface AccountDataDao {
    @Query(
        """
        DELETE FROM memo
        WHERE EXISTS (
            SELECT 1 FROM account_memo
            WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_memo
            WHERE account_memo.memo_id = memo.id AND account_memo.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMemo(accountId: Uuid)

    @Query("DELETE FROM account_memo WHERE account_id = :accountId")
    suspend fun deleteAccountMemo(accountId: Uuid)

    @Query(
        """
        DELETE FROM tag
        WHERE EXISTS (
            SELECT 1 FROM account_tag
            WHERE account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_tag
            WHERE account_tag.tag_id = tag.id AND account_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteTag(accountId: Uuid)

    @Query("DELETE FROM account_tag WHERE account_id = :accountId")
    suspend fun deleteAccountTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM place
        WHERE EXISTS (
            SELECT 1 FROM account_place
            WHERE account_place.place_id = place.id AND account_place.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_place
            WHERE account_place.place_id = place.id AND account_place.account_id != :accountId
        )
        """,
    )
    suspend fun deletePlace(accountId: Uuid)

    @Query("DELETE FROM account_place WHERE account_id = :accountId")
    suspend fun deleteAccountPlace(accountId: Uuid)

    @Query(
        """
        DELETE FROM web
        WHERE EXISTS (
            SELECT 1 FROM account_web
            WHERE account_web.web_id = web.id AND account_web.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_web
            WHERE account_web.web_id = web.id AND account_web.account_id != :accountId
        )
        """,
    )
    suspend fun deleteWeb(accountId: Uuid)

    @Query("DELETE FROM account_web WHERE account_id = :accountId")
    suspend fun deleteAccountWeb(accountId: Uuid)

    @Query(
        """
        DELETE FROM contact
        WHERE EXISTS (
            SELECT 1 FROM account_contact
            WHERE account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_contact
            WHERE account_contact.contact_id = contact.id AND account_contact.account_id != :accountId
        )
        """,
    )
    suspend fun deleteContact(accountId: Uuid)

    @Query("DELETE FROM account_contact WHERE account_id = :accountId")
    suspend fun deleteAccountContact(accountId: Uuid)

    @Query(
        """
        DELETE FROM music
        WHERE EXISTS (
            SELECT 1 FROM account_music
            WHERE account_music.music_id = music.id AND account_music.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_music
            WHERE account_music.music_id = music.id AND account_music.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMusic(accountId: Uuid)

    @Query("DELETE FROM account_music WHERE account_id = :accountId")
    suspend fun deleteAccountMusic(accountId: Uuid)

    @Query(
        """
        DELETE FROM qr
        WHERE EXISTS (
            SELECT 1 FROM account_qr
            WHERE account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_qr
            WHERE account_qr.qr_id = qr.id AND account_qr.account_id != :accountId
        )
        """,
    )
    suspend fun deleteQr(accountId: Uuid)

    @Query("DELETE FROM account_qr WHERE account_id = :accountId")
    suspend fun deleteAccountQr(accountId: Uuid)

    @Query(
        """
        DELETE FROM checklist_item
        WHERE EXISTS (
            SELECT 1 FROM account_checklist_item
            WHERE account_checklist_item.checklist_item_id = checklist_item.id AND account_checklist_item.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_checklist_item
            WHERE account_checklist_item.checklist_item_id = checklist_item.id AND account_checklist_item.account_id != :accountId
        )
        """,
    )
    suspend fun deleteChecklistItem(accountId: Uuid)

    @Query("DELETE FROM account_checklist_item WHERE account_id = :accountId")
    suspend fun deleteAccountChecklistItem(accountId: Uuid)

    @Query(
        """
        DELETE FROM checklist
        WHERE EXISTS (
            SELECT 1 FROM account_checklist
            WHERE account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_checklist
            WHERE account_checklist.checklist_id = checklist.id AND account_checklist.account_id != :accountId
        )
        """,
    )
    suspend fun deleteChecklist(accountId: Uuid)

    @Query("DELETE FROM account_checklist WHERE account_id = :accountId")
    suspend fun deleteAccountChecklist(accountId: Uuid)

    @Query(
        """
        DELETE FROM dday
        WHERE EXISTS (
            SELECT 1 FROM account_dday
            WHERE account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_dday
            WHERE account_dday.dday_id = dday.id AND account_dday.account_id != :accountId
        )
        """,
    )
    suspend fun deleteDDay(accountId: Uuid)

    @Query("DELETE FROM account_dday WHERE account_id = :accountId")
    suspend fun deleteAccountDDay(accountId: Uuid)

    @Query(
        """
        DELETE FROM routine
        WHERE EXISTS (
            SELECT 1 FROM account_routine
            WHERE account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_routine
            WHERE account_routine.routine_id = routine.id AND account_routine.account_id != :accountId
        )
        """,
    )
    suspend fun deleteRoutine(accountId: Uuid)

    @Query("DELETE FROM account_routine WHERE account_id = :accountId")
    suspend fun deleteAccountRoutine(accountId: Uuid)

    @Query(
        """
        DELETE FROM routine_record
        WHERE EXISTS (
            SELECT 1 FROM account_routine_record
            WHERE account_routine_record.routine_record_id = routine_record.id
                AND account_routine_record.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_routine_record
            WHERE account_routine_record.routine_record_id = routine_record.id
                AND account_routine_record.account_id != :accountId
        )
        """,
    )
    suspend fun deleteRoutineRecord(accountId: Uuid)

    @Query("DELETE FROM account_routine_record WHERE account_id = :accountId")
    suspend fun deleteAccountRoutineRecord(accountId: Uuid)

    @Query(
        """
        DELETE FROM memo_tag
        WHERE EXISTS (
            SELECT 1 FROM account_memo_tag
            WHERE account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_memo_tag
            WHERE account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMemoTag(accountId: Uuid)

    @Query("DELETE FROM account_memo_tag WHERE account_id = :accountId")
    suspend fun deleteAccountMemoTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM memo_place
        WHERE EXISTS (
            SELECT 1 FROM account_memo_place
            WHERE account_memo_place.memo_id = memo_place.memo_id
                AND account_memo_place.place_id = memo_place.place_id
                AND account_memo_place.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_memo_place
            WHERE account_memo_place.memo_id = memo_place.memo_id
                AND account_memo_place.place_id = memo_place.place_id
                AND account_memo_place.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMemoPlace(accountId: Uuid)

    @Query("DELETE FROM account_memo_place WHERE account_id = :accountId")
    suspend fun deleteAccountMemoPlace(accountId: Uuid)

    @Query(
        """
        DELETE FROM memo_web
        WHERE EXISTS (
            SELECT 1 FROM account_memo_web
            WHERE account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_memo_web
            WHERE account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMemoWeb(accountId: Uuid)

    @Query("DELETE FROM account_memo_web WHERE account_id = :accountId")
    suspend fun deleteAccountMemoWeb(accountId: Uuid)

    @Query(
        """
        DELETE FROM memo_contact
        WHERE EXISTS (
            SELECT 1 FROM account_memo_contact
            WHERE account_memo_contact.memo_id = memo_contact.memo_id
                AND account_memo_contact.contact_id = memo_contact.contact_id
                AND account_memo_contact.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_memo_contact
            WHERE account_memo_contact.memo_id = memo_contact.memo_id
                AND account_memo_contact.contact_id = memo_contact.contact_id
                AND account_memo_contact.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMemoContact(accountId: Uuid)

    @Query("DELETE FROM account_memo_contact WHERE account_id = :accountId")
    suspend fun deleteAccountMemoContact(accountId: Uuid)

    @Query(
        """
        DELETE FROM tag_link
        WHERE EXISTS (
            SELECT 1 FROM account_tag_link
            WHERE account_tag_link.from_tag_id = tag_link.from_tag_id
                AND account_tag_link.to_tag_id = tag_link.to_tag_id
                AND account_tag_link.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_tag_link
            WHERE account_tag_link.from_tag_id = tag_link.from_tag_id
                AND account_tag_link.to_tag_id = tag_link.to_tag_id
                AND account_tag_link.account_id != :accountId
        )
        """,
    )
    suspend fun deleteTagLink(accountId: Uuid)

    @Query("DELETE FROM account_tag_link WHERE account_id = :accountId")
    suspend fun deleteAccountTagLink(accountId: Uuid)

    @Query(
        """
        DELETE FROM web_tag
        WHERE EXISTS (
            SELECT 1 FROM account_web_tag
            WHERE account_web_tag.web_id = web_tag.web_id
                AND account_web_tag.tag_id = web_tag.tag_id
                AND account_web_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_web_tag
            WHERE account_web_tag.web_id = web_tag.web_id
                AND account_web_tag.tag_id = web_tag.tag_id
                AND account_web_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteWebTag(accountId: Uuid)

    @Query("DELETE FROM account_web_tag WHERE account_id = :accountId")
    suspend fun deleteAccountWebTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM place_tag
        WHERE EXISTS (
            SELECT 1 FROM account_place_tag
            WHERE account_place_tag.place_id = place_tag.place_id
                AND account_place_tag.tag_id = place_tag.tag_id
                AND account_place_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_place_tag
            WHERE account_place_tag.place_id = place_tag.place_id
                AND account_place_tag.tag_id = place_tag.tag_id
                AND account_place_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deletePlaceTag(accountId: Uuid)

    @Query("DELETE FROM account_place_tag WHERE account_id = :accountId")
    suspend fun deleteAccountPlaceTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM contact_tag
        WHERE EXISTS (
            SELECT 1 FROM account_contact_tag
            WHERE account_contact_tag.contact_id = contact_tag.contact_id
                AND account_contact_tag.tag_id = contact_tag.tag_id
                AND account_contact_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_contact_tag
            WHERE account_contact_tag.contact_id = contact_tag.contact_id
                AND account_contact_tag.tag_id = contact_tag.tag_id
                AND account_contact_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteContactTag(accountId: Uuid)

    @Query("DELETE FROM account_contact_tag WHERE account_id = :accountId")
    suspend fun deleteAccountContactTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM checklist_tag
        WHERE EXISTS (
            SELECT 1 FROM account_checklist_tag
            WHERE account_checklist_tag.checklist_id = checklist_tag.checklist_id
                AND account_checklist_tag.tag_id = checklist_tag.tag_id
                AND account_checklist_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_checklist_tag
            WHERE account_checklist_tag.checklist_id = checklist_tag.checklist_id
                AND account_checklist_tag.tag_id = checklist_tag.tag_id
                AND account_checklist_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteChecklistTag(accountId: Uuid)

    @Query("DELETE FROM account_checklist_tag WHERE account_id = :accountId")
    suspend fun deleteAccountChecklistTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM music_tag
        WHERE EXISTS (
            SELECT 1 FROM account_music_tag
            WHERE account_music_tag.music_id = music_tag.music_id
                AND account_music_tag.tag_id = music_tag.tag_id
                AND account_music_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_music_tag
            WHERE account_music_tag.music_id = music_tag.music_id
                AND account_music_tag.tag_id = music_tag.tag_id
                AND account_music_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteMusicTag(accountId: Uuid)

    @Query("DELETE FROM account_music_tag WHERE account_id = :accountId")
    suspend fun deleteAccountMusicTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM qr_tag
        WHERE EXISTS (
            SELECT 1 FROM account_qr_tag
            WHERE account_qr_tag.qr_id = qr_tag.qr_id
                AND account_qr_tag.tag_id = qr_tag.tag_id
                AND account_qr_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_qr_tag
            WHERE account_qr_tag.qr_id = qr_tag.qr_id
                AND account_qr_tag.tag_id = qr_tag.tag_id
                AND account_qr_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteQrTag(accountId: Uuid)

    @Query("DELETE FROM account_qr_tag WHERE account_id = :accountId")
    suspend fun deleteAccountQrTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM dday_tag
        WHERE EXISTS (
            SELECT 1 FROM account_dday_tag
            WHERE account_dday_tag.dday_id = dday_tag.dday_id
                AND account_dday_tag.tag_id = dday_tag.tag_id
                AND account_dday_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_dday_tag
            WHERE account_dday_tag.dday_id = dday_tag.dday_id
                AND account_dday_tag.tag_id = dday_tag.tag_id
                AND account_dday_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteDDayTag(accountId: Uuid)

    @Query("DELETE FROM account_dday_tag WHERE account_id = :accountId")
    suspend fun deleteAccountDDayTag(accountId: Uuid)

    @Query(
        """
        DELETE FROM routine_tag
        WHERE EXISTS (
            SELECT 1 FROM account_routine_tag
            WHERE account_routine_tag.routine_id = routine_tag.routine_id
                AND account_routine_tag.tag_id = routine_tag.tag_id
                AND account_routine_tag.account_id = :accountId
        ) AND NOT EXISTS (
            SELECT 1 FROM account_routine_tag
            WHERE account_routine_tag.routine_id = routine_tag.routine_id
                AND account_routine_tag.tag_id = routine_tag.tag_id
                AND account_routine_tag.account_id != :accountId
        )
        """,
    )
    suspend fun deleteRoutineTag(accountId: Uuid)

    @Query("DELETE FROM account_routine_tag WHERE account_id = :accountId")
    suspend fun deleteAccountRoutineTag(accountId: Uuid)

    @Query("DELETE FROM tag_filter WHERE account_id = :accountId")
    suspend fun deleteTagFilter(accountId: Uuid)

    @Query("DELETE FROM memo_filter_tag WHERE account_id = :accountId")
    suspend fun deleteMemoFilterTag(accountId: Uuid)

    @Query("DELETE FROM calendar_filter_tag WHERE account_id = :accountId")
    suspend fun deleteCalendarFilterTag(accountId: Uuid)

    @Query("DELETE FROM dday_filter_tag WHERE account_id = :accountId")
    suspend fun deleteDDayFilterTag(accountId: Uuid)

    @Query("DELETE FROM routine_filter_tag WHERE account_id = :accountId")
    suspend fun deleteRoutineFilterTag(accountId: Uuid)

    @Query("DELETE FROM routine_filter WHERE account_id = :accountId")
    suspend fun deleteRoutineFilter(accountId: Uuid)

    @Query("DELETE FROM sync_cursor WHERE account_id = :accountId")
    suspend fun deleteSyncCursor(accountId: Uuid)
}
