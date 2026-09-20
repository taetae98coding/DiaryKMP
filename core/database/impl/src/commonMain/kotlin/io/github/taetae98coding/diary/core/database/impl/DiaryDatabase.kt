package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.converter.ContactBirthdayCalendarColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.ContactPhoneNumberListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.WebHeaderListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.dao.AccountCalendarContactBirthdayDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountCalendarMemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountContactDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountContactSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountDailyMemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountDataDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoPlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoPlaceSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoWebDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMemoWebSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMusicDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountMusicSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountPlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountPlaceSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountPlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountPlaceTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagFilterDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagLinkDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagLinkSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagMemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagPlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountTagWebDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountWebDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountWebSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountWebTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.AccountWebTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.dao.CalendarFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.ContactDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoExistenceFilterDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoPlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.MemoWebDao
import io.github.taetae98coding.diary.core.database.impl.dao.MusicDao
import io.github.taetae98coding.diary.core.database.impl.dao.PlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.PlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.SearchMemoDao
import io.github.taetae98coding.diary.core.database.impl.dao.SearchPlaceDao
import io.github.taetae98coding.diary.core.database.impl.dao.SearchTagDao
import io.github.taetae98coding.diary.core.database.impl.dao.SearchWebDao
import io.github.taetae98coding.diary.core.database.impl.dao.SyncCursorDao
import io.github.taetae98coding.diary.core.database.impl.dao.SyncPendingDao
import io.github.taetae98coding.diary.core.database.impl.dao.TagDao
import io.github.taetae98coding.diary.core.database.impl.dao.TagLinkDao
import io.github.taetae98coding.diary.core.database.impl.dao.WebDao
import io.github.taetae98coding.diary.core.database.impl.dao.WebTagDao
import io.github.taetae98coding.diary.core.database.impl.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.library.room3.converter.InstantColumnTypeConverter
import io.github.taetae98coding.diary.library.room3.converter.LocalDateColumnTypeConverter
import io.github.taetae98coding.diary.library.room3.converter.LocalDateTimeColumnTypeConverter
import io.github.taetae98coding.diary.library.room3.converter.UuidColumnTypeConverter

@Database(
    entities = [
        MemoLocalEntity::class,
        AccountMemoLocalEntity::class,
        TagLocalEntity::class,
        AccountTagLocalEntity::class,
        TagLinkLocalEntity::class,
        AccountTagLinkLocalEntity::class,
        TagFilterLocalEntity::class,
        MemoTagLocalEntity::class,
        AccountMemoTagLocalEntity::class,
        MemoFilterTagLocalEntity::class,
        MemoExistenceFilterLocalEntity::class,
        CalendarFilterTagLocalEntity::class,
        PlaceLocalEntity::class,
        AccountPlaceLocalEntity::class,
        PlaceTagLocalEntity::class,
        AccountPlaceTagLocalEntity::class,
        MemoPlaceLocalEntity::class,
        AccountMemoPlaceLocalEntity::class,
        MemoWebLocalEntity::class,
        AccountMemoWebLocalEntity::class,
        WebLocalEntity::class,
        AccountWebLocalEntity::class,
        WebTagLocalEntity::class,
        AccountWebTagLocalEntity::class,
        ContactLocalEntity::class,
        AccountContactLocalEntity::class,
        MusicLocalEntity::class,
        AccountMusicLocalEntity::class,
        SyncCursorLocalEntity::class,
    ],
    version = 1,
)
@ConstructedBy(DiaryDatabaseConstructor::class)
@ColumnTypeConverters(
    UuidColumnTypeConverter::class,
    InstantColumnTypeConverter::class,
    LocalDateColumnTypeConverter::class,
    LocalDateTimeColumnTypeConverter::class,
    WebHeaderListColumnTypeConverter::class,
    ContactPhoneNumberListColumnTypeConverter::class,
    ContactBirthdayCalendarColumnTypeConverter::class,
)
@Suppress("TooManyFunctions")
internal abstract class DiaryDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    abstract fun accountMemoDao(): AccountMemoDao

    abstract fun accountTagMemoDao(): AccountTagMemoDao

    abstract fun accountCalendarMemoDao(): AccountCalendarMemoDao

    abstract fun accountDailyMemoDao(): AccountDailyMemoDao

    abstract fun accountMemoSyncDao(): AccountMemoSyncDao

    abstract fun tagDao(): TagDao

    abstract fun accountTagDao(): AccountTagDao

    abstract fun accountTagFilterDao(): AccountTagFilterDao

    abstract fun accountTagSyncDao(): AccountTagSyncDao

    abstract fun tagLinkDao(): TagLinkDao

    abstract fun accountTagLinkDao(): AccountTagLinkDao

    abstract fun accountTagLinkSyncDao(): AccountTagLinkSyncDao

    abstract fun memoTagDao(): MemoTagDao

    abstract fun memoPlaceDao(): MemoPlaceDao

    abstract fun accountMemoTagDao(): AccountMemoTagDao

    abstract fun accountMemoTagSyncDao(): AccountMemoTagSyncDao

    abstract fun memoFilterTagDao(): MemoFilterTagDao

    abstract fun memoExistenceFilterDao(): MemoExistenceFilterDao

    abstract fun calendarFilterTagDao(): CalendarFilterTagDao

    abstract fun placeDao(): PlaceDao

    abstract fun accountPlaceDao(): AccountPlaceDao

    abstract fun accountPlaceSyncDao(): AccountPlaceSyncDao

    abstract fun placeTagDao(): PlaceTagDao

    abstract fun accountPlaceTagDao(): AccountPlaceTagDao

    abstract fun accountPlaceTagSyncDao(): AccountPlaceTagSyncDao

    abstract fun accountTagPlaceDao(): AccountTagPlaceDao

    abstract fun accountMemoPlaceDao(): AccountMemoPlaceDao

    abstract fun accountMemoPlaceSyncDao(): AccountMemoPlaceSyncDao

    abstract fun memoWebDao(): MemoWebDao

    abstract fun accountMemoWebDao(): AccountMemoWebDao

    abstract fun accountMemoWebSyncDao(): AccountMemoWebSyncDao

    abstract fun searchMemoDao(): SearchMemoDao

    abstract fun searchTagDao(): SearchTagDao

    abstract fun searchPlaceDao(): SearchPlaceDao

    abstract fun searchWebDao(): SearchWebDao

    abstract fun webDao(): WebDao

    abstract fun accountWebDao(): AccountWebDao

    abstract fun accountWebSyncDao(): AccountWebSyncDao

    abstract fun webTagDao(): WebTagDao

    abstract fun accountWebTagDao(): AccountWebTagDao

    abstract fun accountWebTagSyncDao(): AccountWebTagSyncDao

    abstract fun accountTagWebDao(): AccountTagWebDao

    abstract fun contactDao(): ContactDao

    abstract fun accountContactDao(): AccountContactDao

    abstract fun accountContactSyncDao(): AccountContactSyncDao

    abstract fun accountCalendarContactBirthdayDao(): AccountCalendarContactBirthdayDao

    abstract fun musicDao(): MusicDao

    abstract fun accountMusicDao(): AccountMusicDao

    abstract fun accountMusicSyncDao(): AccountMusicSyncDao

    abstract fun syncCursorDao(): SyncCursorDao

    abstract fun syncPendingDao(): SyncPendingDao

    abstract fun accountDataDao(): AccountDataDao

    companion object {
        const val NAME: String = "diary.db"
    }
}
