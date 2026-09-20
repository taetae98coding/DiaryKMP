package io.github.taetae98coding.diary.core.database.impl.dao

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

    @Query("DELETE FROM tag_filter WHERE account_id = :accountId")
    suspend fun deleteTagFilter(accountId: Uuid)

    @Query("DELETE FROM memo_filter_tag WHERE account_id = :accountId")
    suspend fun deleteMemoFilterTag(accountId: Uuid)

    @Query("DELETE FROM calendar_filter_tag WHERE account_id = :accountId")
    suspend fun deleteCalendarFilterTag(accountId: Uuid)

    @Query("DELETE FROM sync_cursor WHERE account_id = :accountId")
    suspend fun deleteSyncCursor(accountId: Uuid)
}
