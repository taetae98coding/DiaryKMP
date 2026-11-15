package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoDetail
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemoEpochDateTime
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.appActionMemoDateTimeInput
import io.github.taetae98coding.diary.work.appaction.memo.toEpochDateTime

public data class DiaryAppActionMemo(
    val id: String,
    val title: String,
    val memoDescription: String,
    val color: String,
    val dateTime: DiaryAppActionMemoDateTime?,
    val primaryTagId: String?,
    val isFinished: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

public data class DiaryAppActionMemoDateTime(
    val allDay: Boolean,
    val startEpochMillis: Long,
    val endInclusiveEpochMillis: Long,
)

public data class DiaryAppActionMemoDetail(
    val memo: DiaryAppActionMemo,
    val tags: List<DiaryAppActionTag>,
    val places: List<DiaryAppActionPlace>,
    val webs: List<DiaryAppActionWeb>,
    val contacts: List<DiaryAppActionContact>,
)

public object DiaryMemoAppAction {
    public suspend fun get(memoId: String): DiaryAppActionMemoDetail? = memoAppAction().get(memoId = memoId)?.toDiary()

    public suspend fun getIncludingDeleted(memoId: String): DiaryAppActionMemoDetail? = memoAppAction().getIncludingDeleted(memoId = memoId)?.toDiary()

    public suspend fun add(
        title: String,
        memoDescription: String?,
        color: String?,
        allDay: Boolean?,
        startEpochMillis: Long?,
        endInclusiveEpochMillis: Long?,
        primaryTagId: String?,
        tagIds: List<String>,
        placeIds: List<String>,
        webIds: List<String>,
        contactIds: List<String>,
    ): String =
        memoAppAction()
            .add(
                title = title,
                description = memoDescription,
                color = color,
                dateTime = appActionMemoDateTimeInput(allDay = allDay, startEpochMillis = startEpochMillis, endInclusiveEpochMillis = endInclusiveEpochMillis),
                primaryTagId = primaryTagId,
                tagIds = tagIds,
                placeIds = placeIds,
                webIds = webIds,
                contactIds = contactIds,
            ).id

    public suspend fun update(
        memoId: String,
        title: String?,
        memoDescription: String?,
        color: String?,
        allDay: Boolean?,
        startEpochMillis: Long?,
        endInclusiveEpochMillis: Long?,
        clearDateTime: Boolean?,
    ): Int =
        memoAppAction()
            .update(
                memoId = memoId,
                title = title,
                description = memoDescription,
                color = color,
                dateTime = appActionMemoDateTimeInput(allDay = allDay, startEpochMillis = startEpochMillis, endInclusiveEpochMillis = endInclusiveEpochMillis),
                clearDateTime = clearDateTime,
            ).changedCount

    public suspend fun copy(memoId: String): String = memoAppAction().copy(memoId = memoId).id

    public suspend fun finish(memoId: String): Int = memoAppAction().finish(memoId = memoId).changedCount

    public suspend fun restart(memoId: String): Int = memoAppAction().restart(memoId = memoId).changedCount

    public suspend fun delete(memoId: String): Int = memoAppAction().delete(memoId = memoId).changedCount

    public suspend fun restore(memoId: String): Int = memoAppAction().restore(memoId = memoId).changedCount
}

private fun memoAppAction(): MemoAppAction = diaryAppAction()

private fun AppActionMemoDetail.toDiary(): DiaryAppActionMemoDetail =
    DiaryAppActionMemoDetail(
        memo = memo.toDiary(),
        tags = tags.map { tag -> tag.toDiary() },
        places = places.map { place -> place.toDiary() },
        webs = webs.map { web -> web.toDiary() },
        contacts = contacts.map { contact -> contact.toDiary() },
    )

internal fun AppActionMemo.toDiary(): DiaryAppActionMemo =
    DiaryAppActionMemo(
        id = id,
        title = title,
        memoDescription = description,
        color = color,
        dateTime = dateTime?.toEpochDateTime()?.toDiary(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        createdAtEpochMillis = appActionEpochMillis(instant = createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = updatedAt),
    )

internal fun AppActionMemoEpochDateTime.toDiary(): DiaryAppActionMemoDateTime = DiaryAppActionMemoDateTime(allDay = allDay, startEpochMillis = startEpochMillis, endInclusiveEpochMillis = endInclusiveEpochMillis)
