package io.github.taetae98coding.diary.core.database.impl.spotlight.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
internal interface SpotlightDao {
    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.is_deleted = 0
        ORDER BY memo.id ASC
        """,
    )
    fun getMemoList(accountId: Uuid): Flow<List<MemoLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
        ORDER BY tag.id ASC
        """,
    )
    fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0
        ORDER BY place.id ASC
        """,
    )
    fun getPlaceList(accountId: Uuid): Flow<List<PlaceLocalEntity>>

    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0
        ORDER BY web.id ASC
        """,
    )
    fun getWebList(accountId: Uuid): Flow<List<WebLocalEntity>>

    @Query(
        """
        SELECT contact.*
        FROM contact
        INNER JOIN account_contact
            ON account_contact.contact_id = contact.id AND account_contact.account_id = :accountId
        WHERE contact.is_deleted = 0
        ORDER BY contact.id ASC
        """,
    )
    fun getContactList(accountId: Uuid): Flow<List<ContactLocalEntity>>

    @Query(
        """
        SELECT checklist.*
        FROM checklist
        INNER JOIN account_checklist
            ON account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        WHERE checklist.is_deleted = 0
        ORDER BY checklist.id ASC
        """,
    )
    fun getChecklistList(accountId: Uuid): Flow<List<ChecklistLocalEntity>>

    @Query(
        """
        SELECT qr.*
        FROM qr
        INNER JOIN account_qr
            ON account_qr.qr_id = qr.id AND account_qr.account_id = :accountId
        WHERE qr.is_deleted = 0
        ORDER BY qr.id ASC
        """,
    )
    fun getQrList(accountId: Uuid): Flow<List<QrLocalEntity>>

    @Query(
        """
        SELECT music.*
        FROM music
        INNER JOIN account_music
            ON account_music.music_id = music.id AND account_music.account_id = :accountId
        WHERE music.is_deleted = 0
        ORDER BY music.id ASC
        """,
    )
    fun getMusicList(accountId: Uuid): Flow<List<MusicLocalEntity>>

    @Query(
        """
        SELECT dday.*
        FROM dday
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE dday.is_deleted = 0
        ORDER BY dday.id ASC
        """,
    )
    fun getDDayList(accountId: Uuid): Flow<List<DDayLocalEntity>>

    @Query(
        """
        SELECT routine.*
        FROM routine
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
        WHERE routine.is_deleted = 0
        ORDER BY routine.id ASC
        """,
    )
    fun getRoutineList(accountId: Uuid): Flow<List<RoutineLocalEntity>>
}
