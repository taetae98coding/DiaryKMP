package io.github.taetae98coding.diary.core.database.impl.sync.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import kotlin.uuid.Uuid

@Entity(
    tableName = "sync_cursor",
    primaryKeys = ["account_id", "kind"],
)
internal data class SyncCursorLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "kind", defaultValue = "''")
    val kind: String,
    @ColumnInfo(name = "usn", defaultValue = "0")
    val usn: Long,
) {
    companion object {
        fun column(kind: SyncKind): String =
            when (kind) {
                SyncKind.MEMO -> "memo"
                SyncKind.TAG -> "tag"
                SyncKind.PLACE -> "place"
                SyncKind.WEB -> "web"
                SyncKind.CONTACT -> "contact"
                SyncKind.MUSIC -> "music"
                SyncKind.MEMO_TAG -> "memo_tag"
                SyncKind.MEMO_PLACE -> "memo_place"
                SyncKind.MEMO_WEB -> "memo_web"
                SyncKind.MEMO_CONTACT -> "memo_contact"
                SyncKind.TAG_LINK -> "tag_link"
                SyncKind.WEB_TAG -> "web_tag"
                SyncKind.PLACE_TAG -> "place_tag"
            }
    }
}
