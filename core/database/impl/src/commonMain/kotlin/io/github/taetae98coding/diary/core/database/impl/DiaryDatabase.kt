package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
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
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.dao.CalendarFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountCalendarContactBirthdayDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountContactDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountContactSyncDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.ContactDao
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.converter.ContactBirthdayCalendarColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.ContactPhoneNumberListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.WebHeaderListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountCalendarMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountDailyMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountMemoSyncDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountTagMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.MemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memocontact.dao.AccountMemoContactDao
import io.github.taetae98coding.diary.core.database.impl.memocontact.dao.AccountMemoContactSyncDao
import io.github.taetae98coding.diary.core.database.impl.memocontact.dao.MemoContactDao
import io.github.taetae98coding.diary.core.database.impl.memocontact.entity.AccountMemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memofilter.dao.MemoExistenceFilterDao
import io.github.taetae98coding.diary.core.database.impl.memofilter.dao.MemoFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.memofilter.entity.MemoFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoplace.dao.AccountMemoPlaceDao
import io.github.taetae98coding.diary.core.database.impl.memoplace.dao.AccountMemoPlaceSyncDao
import io.github.taetae98coding.diary.core.database.impl.memoplace.dao.MemoPlaceDao
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.dao.AccountMemoTagDao
import io.github.taetae98coding.diary.core.database.impl.memotag.dao.AccountMemoTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.memotag.dao.MemoTagDao
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.dao.AccountMemoWebDao
import io.github.taetae98coding.diary.core.database.impl.memoweb.dao.AccountMemoWebSyncDao
import io.github.taetae98coding.diary.core.database.impl.memoweb.dao.MemoWebDao
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.music.dao.AccountMusicDao
import io.github.taetae98coding.diary.core.database.impl.music.dao.AccountMusicSyncDao
import io.github.taetae98coding.diary.core.database.impl.music.dao.MusicDao
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountPlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountPlaceSyncDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountTagPlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.PlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.AccountPlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.AccountPlaceTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.PlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchMemoDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchPlaceDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchTagDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchWebDao
import io.github.taetae98coding.diary.core.database.impl.sync.dao.AccountDataDao
import io.github.taetae98coding.diary.core.database.impl.sync.dao.SyncCursorDao
import io.github.taetae98coding.diary.core.database.impl.sync.dao.SyncPendingDao
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.AccountTagDao
import io.github.taetae98coding.diary.core.database.impl.tag.dao.AccountTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.tag.dao.TagDao
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tagfilter.dao.AccountTagFilterDao
import io.github.taetae98coding.diary.core.database.impl.taglink.dao.AccountTagLinkDao
import io.github.taetae98coding.diary.core.database.impl.taglink.dao.AccountTagLinkSyncDao
import io.github.taetae98coding.diary.core.database.impl.taglink.dao.TagLinkDao
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.dao.AccountTagWebDao
import io.github.taetae98coding.diary.core.database.impl.web.dao.AccountWebDao
import io.github.taetae98coding.diary.core.database.impl.web.dao.AccountWebSyncDao
import io.github.taetae98coding.diary.core.database.impl.web.dao.WebDao
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.dao.AccountWebTagDao
import io.github.taetae98coding.diary.core.database.impl.webtag.dao.AccountWebTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.webtag.dao.WebTagDao
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
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
        MemoContactLocalEntity::class,
        AccountMemoContactLocalEntity::class,
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

    abstract fun memoContactDao(): MemoContactDao

    abstract fun accountMemoContactDao(): AccountMemoContactDao

    abstract fun accountMemoContactSyncDao(): AccountMemoContactSyncDao

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
