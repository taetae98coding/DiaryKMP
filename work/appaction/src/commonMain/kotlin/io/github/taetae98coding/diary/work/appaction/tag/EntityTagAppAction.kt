package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.checklist.usecase.FindChecklistUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.qr.usecase.FindQrUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionDone
import io.github.taetae98coding.diary.work.appaction.core.appActionNotDone
import io.github.taetae98coding.diary.work.appaction.core.isActive
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

public enum class AppActionEntityTagKind(
    internal val kind: EntityTagKind,
    internal val entityIdName: String,
) {
    PLACE(kind = EntityTagKind.PLACE, entityIdName = "placeId"),
    WEB(kind = EntityTagKind.WEB, entityIdName = "webId"),
    CONTACT(kind = EntityTagKind.CONTACT, entityIdName = "contactId"),
    CHECKLIST(kind = EntityTagKind.CHECKLIST, entityIdName = "checklistId"),
    QR(kind = EntityTagKind.QR, entityIdName = "qrId"),
    MUSIC(kind = EntityTagKind.MUSIC, entityIdName = "musicId"),
    DDAY(kind = EntityTagKind.DDAY, entityIdName = "ddayId"),
    ROUTINE(kind = EntityTagKind.ROUTINE, entityIdName = "routineId"),
}

@Factory
public class EntityTagAppAction internal constructor(
    private val findPlaceUseCase: FindPlaceUseCase,
    private val findWebUseCase: FindWebUseCase,
    private val findContactUseCase: FindContactUseCase,
    private val findChecklistUseCase: FindChecklistUseCase,
    private val findQrUseCase: FindQrUseCase,
    private val findMusicUseCase: FindMusicUseCase,
    private val findDDayUseCase: FindDDayUseCase,
    private val findRoutineUseCase: FindRoutineUseCase,
    private val findTagUseCase: FindTagUseCase,
    private val addEntityTagUseCase: AddEntityTagUseCase,
    private val removeEntityTagUseCase: RemoveEntityTagUseCase,
) {
    public suspend fun add(
        kind: AppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): AppActionDone =
        link(kind = kind, entityId = entityId, tagId = tagId) { entity, tag ->
            addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = kind.kind, entityId = entity, tagId = tag)).getOrThrow()
        }

    public suspend fun remove(
        kind: AppActionEntityTagKind,
        entityId: String,
        tagId: String,
    ): AppActionDone =
        link(kind = kind, entityId = entityId, tagId = tagId) { entity, tag ->
            removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = kind.kind, entityId = entity, tagId = tag)).getOrThrow()
        }

    private suspend fun link(
        kind: AppActionEntityTagKind,
        entityId: String,
        tagId: String,
        block: suspend (entityId: Uuid, tagId: Uuid) -> Unit,
    ): AppActionDone =
        runAppAction {
            val entity = entityId.toUuidArgument(name = kind.entityIdName)
            val tag = tagId.toUuidArgument(name = TAG_ID)
            val isLinkable = isEntityActive(kind = kind, id = entity) && findTagUseCase.isActive(id = tag, isDeleted = Tag::isDeleted)

            if (!isLinkable) return@runAppAction appActionNotDone

            block(entity, tag)
            appActionDone
        }

    private suspend fun isEntityActive(
        kind: AppActionEntityTagKind,
        id: Uuid,
    ): Boolean =
        when (kind) {
            AppActionEntityTagKind.PLACE -> findPlaceUseCase.isActive(id = id, isDeleted = Place::isDeleted)
            AppActionEntityTagKind.WEB -> findWebUseCase.isActive(id = id, isDeleted = Web::isDeleted)
            AppActionEntityTagKind.CONTACT -> findContactUseCase.isActive(id = id, isDeleted = Contact::isDeleted)
            AppActionEntityTagKind.CHECKLIST -> findChecklistUseCase.isActive(id = id, isDeleted = Checklist::isDeleted)
            AppActionEntityTagKind.QR -> findQrUseCase.isActive(id = id, isDeleted = Qr::isDeleted)
            AppActionEntityTagKind.MUSIC -> findMusicUseCase.isActive(id = id, isDeleted = Music::isDeleted)
            AppActionEntityTagKind.DDAY -> findDDayUseCase.isActive(id = id, isDeleted = DDay::isDeleted)
            AppActionEntityTagKind.ROUTINE -> findRoutineUseCase.isActive(id = id, isDeleted = Routine::isDeleted)
        }
}
