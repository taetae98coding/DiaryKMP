package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.dao.CalendarFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.calendarfilter.entity.CalendarFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.dao.AccountChecklistDao
import io.github.taetae98coding.diary.core.database.impl.checklist.dao.AccountChecklistSyncDao
import io.github.taetae98coding.diary.core.database.impl.checklist.dao.AccountTagChecklistDao
import io.github.taetae98coding.diary.core.database.impl.checklist.dao.ChecklistDao
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.dao.AccountChecklistItemDao
import io.github.taetae98coding.diary.core.database.impl.checklistitem.dao.AccountChecklistItemSyncDao
import io.github.taetae98coding.diary.core.database.impl.checklistitem.dao.ChecklistItemDao
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.dao.AccountChecklistTagDao
import io.github.taetae98coding.diary.core.database.impl.checklisttag.dao.AccountChecklistTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.checklisttag.dao.ChecklistTagDao
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.AccountChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.ChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountCalendarContactBirthdayDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountContactDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountContactSyncDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.AccountTagContactDao
import io.github.taetae98coding.diary.core.database.impl.contact.dao.ContactDao
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.dao.AccountContactTagDao
import io.github.taetae98coding.diary.core.database.impl.contacttag.dao.AccountContactTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.contacttag.dao.ContactTagDao
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.AccountContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.ContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.converter.CalendarDDayKindColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.ContactBirthdayCalendarColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.ContactPhoneNumberListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.DDayOverflowPolicyColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.DDayRepeatListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.RoutineCycleColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.RoutineViewScopeColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.converter.WebHeaderListColumnTypeConverter
import io.github.taetae98coding.diary.core.database.impl.dday.dao.AccountCalendarDDayDao
import io.github.taetae98coding.diary.core.database.impl.dday.dao.AccountDDayDao
import io.github.taetae98coding.diary.core.database.impl.dday.dao.AccountDDaySyncDao
import io.github.taetae98coding.diary.core.database.impl.dday.dao.AccountTagDDayDao
import io.github.taetae98coding.diary.core.database.impl.dday.dao.DDayDao
import io.github.taetae98coding.diary.core.database.impl.dday.entity.AccountDDayLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.dao.DDayFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.dao.AccountDDayTagDao
import io.github.taetae98coding.diary.core.database.impl.ddaytag.dao.AccountDDayTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.ddaytag.dao.DDayTagDao
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.AccountDDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.DDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountCalendarMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountContactMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountMemoSyncDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountPlaceMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountTagMemoDao
import io.github.taetae98coding.diary.core.database.impl.memo.dao.AccountWebMemoDao
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
import io.github.taetae98coding.diary.core.database.impl.migration.ContactBirthYearAutoMigrationSpec
import io.github.taetae98coding.diary.core.database.impl.migration.ContactHowWeMetAutoMigrationSpec
import io.github.taetae98coding.diary.core.database.impl.music.dao.AccountMusicDao
import io.github.taetae98coding.diary.core.database.impl.music.dao.AccountMusicSyncDao
import io.github.taetae98coding.diary.core.database.impl.music.dao.AccountTagMusicDao
import io.github.taetae98coding.diary.core.database.impl.music.dao.MusicDao
import io.github.taetae98coding.diary.core.database.impl.music.entity.AccountMusicLocalEntity
import io.github.taetae98coding.diary.core.database.impl.musictag.dao.AccountMusicTagDao
import io.github.taetae98coding.diary.core.database.impl.musictag.dao.AccountMusicTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.musictag.dao.MusicTagDao
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.AccountMusicTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.MusicTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountPlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountPlaceSyncDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.AccountTagPlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.dao.PlaceDao
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.AccountPlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.AccountPlaceTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.placetag.dao.PlaceTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qr.dao.AccountQrDao
import io.github.taetae98coding.diary.core.database.impl.qr.dao.AccountQrSyncDao
import io.github.taetae98coding.diary.core.database.impl.qr.dao.AccountTagQrDao
import io.github.taetae98coding.diary.core.database.impl.qr.dao.QrDao
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qrtag.dao.AccountQrTagDao
import io.github.taetae98coding.diary.core.database.impl.qrtag.dao.AccountQrTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.qrtag.dao.QrTagDao
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.AccountQrTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.qrtag.entity.QrTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routine.dao.AccountCalendarRoutineRecordDao
import io.github.taetae98coding.diary.core.database.impl.routine.dao.AccountRoutineDao
import io.github.taetae98coding.diary.core.database.impl.routine.dao.AccountRoutineSyncDao
import io.github.taetae98coding.diary.core.database.impl.routine.dao.AccountTagRoutineDao
import io.github.taetae98coding.diary.core.database.impl.routine.dao.RoutineDao
import io.github.taetae98coding.diary.core.database.impl.routine.entity.AccountRoutineLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.dao.RoutineFilterDao
import io.github.taetae98coding.diary.core.database.impl.routinefilter.dao.RoutineFilterTagDao
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinefilter.entity.RoutineFilterTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinerecord.dao.AccountRoutineRecordDao
import io.github.taetae98coding.diary.core.database.impl.routinerecord.dao.AccountRoutineRecordSyncDao
import io.github.taetae98coding.diary.core.database.impl.routinerecord.dao.RoutineRecordDao
import io.github.taetae98coding.diary.core.database.impl.routinerecord.entity.AccountRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.dao.AccountRoutineTagDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.dao.AccountRoutineTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.dao.RoutineTagDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.AccountRoutineTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.RoutineTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchDDayDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchMemoDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchPlaceDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchRoutineDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchTagDao
import io.github.taetae98coding.diary.core.database.impl.search.dao.SearchWebDao
import io.github.taetae98coding.diary.core.database.impl.spotlight.dao.SpotlightDao
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
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.WebTagLocalEntity
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
        QrLocalEntity::class,
        AccountQrLocalEntity::class,
        ChecklistLocalEntity::class,
        AccountChecklistLocalEntity::class,
        ChecklistItemLocalEntity::class,
        AccountChecklistItemLocalEntity::class,
        ContactTagLocalEntity::class,
        AccountContactTagLocalEntity::class,
        ChecklistTagLocalEntity::class,
        AccountChecklistTagLocalEntity::class,
        MusicTagLocalEntity::class,
        AccountMusicTagLocalEntity::class,
        QrTagLocalEntity::class,
        AccountQrTagLocalEntity::class,
        DDayLocalEntity::class,
        AccountDDayLocalEntity::class,
        DDayTagLocalEntity::class,
        AccountDDayTagLocalEntity::class,
        DDayFilterTagLocalEntity::class,
        RoutineLocalEntity::class,
        AccountRoutineLocalEntity::class,
        RoutineRecordLocalEntity::class,
        AccountRoutineRecordLocalEntity::class,
        RoutineTagLocalEntity::class,
        AccountRoutineTagLocalEntity::class,
        RoutineFilterTagLocalEntity::class,
        RoutineFilterLocalEntity::class,
        SyncCursorLocalEntity::class,
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = ContactBirthYearAutoMigrationSpec::class),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = ContactHowWeMetAutoMigrationSpec::class),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
    ],
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
    DDayRepeatListColumnTypeConverter::class,
    DDayOverflowPolicyColumnTypeConverter::class,
    CalendarDDayKindColumnTypeConverter::class,
    RoutineCycleColumnTypeConverter::class,
    RoutineViewScopeColumnTypeConverter::class,
)
@Suppress("TooManyFunctions")
internal abstract class DiaryDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    abstract fun accountMemoDao(): AccountMemoDao

    abstract fun accountTagMemoDao(): AccountTagMemoDao

    abstract fun accountContactMemoDao(): AccountContactMemoDao

    abstract fun accountPlaceMemoDao(): AccountPlaceMemoDao

    abstract fun accountWebMemoDao(): AccountWebMemoDao

    abstract fun accountCalendarMemoDao(): AccountCalendarMemoDao

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

    abstract fun spotlightDao(): SpotlightDao

    abstract fun searchDDayDao(): SearchDDayDao

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

    abstract fun contactTagDao(): ContactTagDao

    abstract fun accountContactTagDao(): AccountContactTagDao

    abstract fun accountContactTagSyncDao(): AccountContactTagSyncDao

    abstract fun accountTagContactDao(): AccountTagContactDao

    abstract fun accountCalendarContactBirthdayDao(): AccountCalendarContactBirthdayDao

    abstract fun musicDao(): MusicDao

    abstract fun accountMusicDao(): AccountMusicDao

    abstract fun accountMusicSyncDao(): AccountMusicSyncDao

    abstract fun musicTagDao(): MusicTagDao

    abstract fun accountMusicTagDao(): AccountMusicTagDao

    abstract fun accountMusicTagSyncDao(): AccountMusicTagSyncDao

    abstract fun accountTagMusicDao(): AccountTagMusicDao

    abstract fun qrDao(): QrDao

    abstract fun accountQrDao(): AccountQrDao

    abstract fun accountQrSyncDao(): AccountQrSyncDao

    abstract fun qrTagDao(): QrTagDao

    abstract fun accountQrTagDao(): AccountQrTagDao

    abstract fun accountQrTagSyncDao(): AccountQrTagSyncDao

    abstract fun accountTagQrDao(): AccountTagQrDao

    abstract fun checklistDao(): ChecklistDao

    abstract fun accountChecklistDao(): AccountChecklistDao

    abstract fun accountChecklistSyncDao(): AccountChecklistSyncDao

    abstract fun checklistTagDao(): ChecklistTagDao

    abstract fun accountChecklistTagDao(): AccountChecklistTagDao

    abstract fun accountChecklistTagSyncDao(): AccountChecklistTagSyncDao

    abstract fun accountTagChecklistDao(): AccountTagChecklistDao

    abstract fun checklistItemDao(): ChecklistItemDao

    abstract fun accountChecklistItemDao(): AccountChecklistItemDao

    abstract fun accountChecklistItemSyncDao(): AccountChecklistItemSyncDao

    abstract fun ddayDao(): DDayDao

    abstract fun accountDDayDao(): AccountDDayDao

    abstract fun accountDDaySyncDao(): AccountDDaySyncDao

    abstract fun ddayTagDao(): DDayTagDao

    abstract fun accountDDayTagDao(): AccountDDayTagDao

    abstract fun accountDDayTagSyncDao(): AccountDDayTagSyncDao

    abstract fun accountTagDDayDao(): AccountTagDDayDao

    abstract fun accountCalendarDDayDao(): AccountCalendarDDayDao

    abstract fun ddayFilterTagDao(): DDayFilterTagDao

    abstract fun routineDao(): RoutineDao

    abstract fun accountRoutineDao(): AccountRoutineDao

    abstract fun accountRoutineSyncDao(): AccountRoutineSyncDao

    abstract fun routineTagDao(): RoutineTagDao

    abstract fun accountRoutineTagDao(): AccountRoutineTagDao

    abstract fun accountRoutineTagSyncDao(): AccountRoutineTagSyncDao

    abstract fun accountTagRoutineDao(): AccountTagRoutineDao

    abstract fun routineRecordDao(): RoutineRecordDao

    abstract fun accountRoutineRecordDao(): AccountRoutineRecordDao

    abstract fun accountRoutineRecordSyncDao(): AccountRoutineRecordSyncDao

    abstract fun accountCalendarRoutineRecordDao(): AccountCalendarRoutineRecordDao

    abstract fun routineFilterTagDao(): RoutineFilterTagDao

    abstract fun routineFilterDao(): RoutineFilterDao

    abstract fun searchRoutineDao(): SearchRoutineDao

    abstract fun syncCursorDao(): SyncCursorDao

    abstract fun syncPendingDao(): SyncPendingDao

    abstract fun accountDataDao(): AccountDataDao

    companion object {
        const val NAME: String = "diary.db"
    }
}
