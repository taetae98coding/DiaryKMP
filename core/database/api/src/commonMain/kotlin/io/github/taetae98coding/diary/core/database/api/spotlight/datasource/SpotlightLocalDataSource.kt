package io.github.taetae98coding.diary.core.database.api.spotlight.datasource

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface SpotlightLocalDataSource {
    public fun getMemoList(accountId: Uuid): Flow<List<MemoLocalEntity>>

    public fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>>

    public fun getPlaceList(accountId: Uuid): Flow<List<PlaceLocalEntity>>

    public fun getWebList(accountId: Uuid): Flow<List<WebLocalEntity>>

    public fun getContactList(accountId: Uuid): Flow<List<ContactLocalEntity>>

    public fun getChecklistList(accountId: Uuid): Flow<List<ChecklistLocalEntity>>

    public fun getQrList(accountId: Uuid): Flow<List<QrLocalEntity>>

    public fun getMusicList(accountId: Uuid): Flow<List<MusicLocalEntity>>

    public fun getDDayList(accountId: Uuid): Flow<List<DDayLocalEntity>>
}
