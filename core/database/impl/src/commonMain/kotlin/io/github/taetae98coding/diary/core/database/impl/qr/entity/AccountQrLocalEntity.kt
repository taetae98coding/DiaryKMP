package io.github.taetae98coding.diary.core.database.impl.qr.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import kotlin.uuid.Uuid

@Entity(
    tableName = "account_qr",
    primaryKeys = ["account_id", "qr_id"],
)
internal data class AccountQrLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "qr_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val qrId: Uuid,
    @ColumnInfo(name = "is_dirty", defaultValue = "1")
    val isDirty: Boolean,
)
