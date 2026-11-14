package io.github.taetae98coding.diary.core.database.impl.spotlight.datasource

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.spotlight.datasource.SpotlightLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SpotlightLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SpotlightLocalDataSource {
    override fun getMemoList(accountId: Uuid): Flow<List<MemoLocalEntity>> = database.spotlightDao().getMemoList(accountId = accountId)

    override fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>> = database.spotlightDao().getTagList(accountId = accountId)

    override fun getPlaceList(accountId: Uuid): Flow<List<PlaceLocalEntity>> = database.spotlightDao().getPlaceList(accountId = accountId)

    override fun getWebList(accountId: Uuid): Flow<List<WebLocalEntity>> = database.spotlightDao().getWebList(accountId = accountId)

    override fun getContactList(accountId: Uuid): Flow<List<ContactLocalEntity>> = database.spotlightDao().getContactList(accountId = accountId)

    override fun getChecklistList(accountId: Uuid): Flow<List<ChecklistLocalEntity>> = database.spotlightDao().getChecklistList(accountId = accountId)

    override fun getQrList(accountId: Uuid): Flow<List<QrLocalEntity>> = database.spotlightDao().getQrList(accountId = accountId)

    override fun getMusicList(accountId: Uuid): Flow<List<MusicLocalEntity>> = database.spotlightDao().getMusicList(accountId = accountId)

    override fun getDDayList(accountId: Uuid): Flow<List<DDayLocalEntity>> = database.spotlightDao().getDDayList(accountId = accountId)

    override fun getRoutineList(accountId: Uuid): Flow<List<RoutineLocalEntity>> = database.spotlightDao().getRoutineList(accountId = accountId)
}
