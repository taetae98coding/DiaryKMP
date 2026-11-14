package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction

public enum class DiaryAppActionEntityTagKind {
    PLACE,
    WEB,
    CONTACT,
    CHECKLIST,
    QR,
    MUSIC,
    DDAY,
    ROUTINE,
}

public object DiaryEntityTagAppAction {
    public suspend fun add(
        kind: DiaryAppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): Boolean = entityTagAppAction().add(kind = kind.toKind(), entityId = entityId, tagId = tagId).done

    public suspend fun remove(
        kind: DiaryAppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): Boolean = entityTagAppAction().remove(kind = kind.toKind(), entityId = entityId, tagId = tagId).done
}

private fun entityTagAppAction(): EntityTagAppAction = diaryAppAction()

private fun DiaryAppActionEntityTagKind.toKind(): AppActionEntityTagKind =
    when (this) {
        DiaryAppActionEntityTagKind.PLACE -> AppActionEntityTagKind.PLACE
        DiaryAppActionEntityTagKind.WEB -> AppActionEntityTagKind.WEB
        DiaryAppActionEntityTagKind.CONTACT -> AppActionEntityTagKind.CONTACT
        DiaryAppActionEntityTagKind.CHECKLIST -> AppActionEntityTagKind.CHECKLIST
        DiaryAppActionEntityTagKind.QR -> AppActionEntityTagKind.QR
        DiaryAppActionEntityTagKind.MUSIC -> AppActionEntityTagKind.MUSIC
        DiaryAppActionEntityTagKind.DDAY -> AppActionEntityTagKind.DDAY
        DiaryAppActionEntityTagKind.ROUTINE -> AppActionEntityTagKind.ROUTINE
    }
