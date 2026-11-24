package io.github.taetae98coding.diary.work.appaction

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.github.taetae98coding.diary.work.appaction.calendar.CalendarAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistAppAction
import io.github.taetae98coding.diary.work.appaction.checklist.ChecklistItemAppAction
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction
import io.github.taetae98coding.diary.work.appaction.dday.DDayAppAction
import io.github.taetae98coding.diary.work.appaction.lunar.LunarAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoAppAction
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.memolink.MemoLinkAppAction
import io.github.taetae98coding.diary.work.appaction.music.MusicAppAction
import io.github.taetae98coding.diary.work.appaction.place.PlaceAppAction
import io.github.taetae98coding.diary.work.appaction.qr.QrAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineListAppAction
import io.github.taetae98coding.diary.work.appaction.routine.RoutineRecordAppAction
import io.github.taetae98coding.diary.work.appaction.search.SearchAppAction
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagItemAppAction
import io.github.taetae98coding.diary.work.appaction.tag.TagLinkAppAction
import io.github.taetae98coding.diary.work.appaction.web.WebAppAction

// 앱 동작마다 accessor 하나씩 두는 목록이라 앱 동작 수만큼 길어진다.
@Suppress("ComplexInterface")
@ContributesTo(AppScope::class)
public interface AppActionGraph {
    public val calendarAppAction: CalendarAppAction
    public val checklistAppAction: ChecklistAppAction
    public val checklistItemAppAction: ChecklistItemAppAction
    public val contactAppAction: ContactAppAction
    public val ddayAppAction: DDayAppAction
    public val entityTagAppAction: EntityTagAppAction
    public val lunarAppAction: LunarAppAction
    public val memoAppAction: MemoAppAction
    public val memoLinkAppAction: MemoLinkAppAction
    public val memoListAppAction: MemoListAppAction
    public val musicAppAction: MusicAppAction
    public val placeAppAction: PlaceAppAction
    public val qrAppAction: QrAppAction
    public val routineAppAction: RoutineAppAction
    public val routineListAppAction: RoutineListAppAction
    public val routineRecordAppAction: RoutineRecordAppAction
    public val searchAppAction: SearchAppAction
    public val tagAppAction: TagAppAction
    public val tagItemAppAction: TagItemAppAction
    public val tagLinkAppAction: TagLinkAppAction
    public val webAppAction: WebAppAction
}
