package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun tagAppActionOf(vararg useCaseArray: Any): TagAppAction {
    val useCaseList = useCaseArray.toList()

    return TagAppAction(
        pageTagHomeUseCase = useCaseList.useCase(),
        pageFinishedTagUseCase = useCaseList.useCase(),
        findTagUseCase = useCaseList.findUseCase { Arb.tag().next().copy(isDeleted = false) },
        getLinkedTagUseCase = useCaseList.useCase(),
        addTagUseCase = useCaseList.useCase(),
        updateTagUseCase = useCaseList.useCase(),
        finishTagUseCase = useCaseList.useCase(),
        restartTagUseCase = useCaseList.useCase(),
        deleteTagUseCase = useCaseList.useCase(),
        restoreTagUseCase = useCaseList.useCase(),
    )
}

internal fun tagLinkAppActionOf(vararg useCaseArray: Any): TagLinkAppAction {
    val useCaseList = useCaseArray.toList()

    return TagLinkAppAction(
        findTagUseCase = useCaseList.findUseCase { Arb.tag().next().copy(isDeleted = false) },
        addTagLinkUseCase = useCaseList.useCase(),
        removeTagLinkUseCase = useCaseList.useCase(),
    )
}

internal fun tagItemAppActionOf(vararg useCaseArray: Any): TagItemAppAction {
    val useCaseList = useCaseArray.toList()

    return TagItemAppAction(
        findTagUseCase = useCaseList.findUseCase { Arb.tag().next().copy(isDeleted = false) },
        pageTagMemoUseCase = useCaseList.useCase(),
        pageFinishedTagMemoUseCase = useCaseList.useCase(),
        pageTagWebUseCase = useCaseList.useCase(),
        pageTagPlaceUseCase = useCaseList.useCase(),
        pageTagContactUseCase = useCaseList.useCase(),
        pageTagChecklistUseCase = useCaseList.useCase(),
        pageTagMusicUseCase = useCaseList.useCase(),
        pageTagQrUseCase = useCaseList.useCase(),
        pageTagDDayUseCase = useCaseList.useCase(),
        clock = useCaseList.useCase(),
    )
}

internal fun entityTagAppActionOf(vararg useCaseArray: Any): EntityTagAppAction {
    val useCaseList = useCaseArray.toList()

    return EntityTagAppAction(
        findPlaceUseCase = useCaseList.findUseCase { Arb.place().next().copy(isDeleted = false) },
        findWebUseCase = useCaseList.findUseCase { Arb.web().next().copy(isDeleted = false) },
        findContactUseCase = useCaseList.findUseCase { Arb.contact().next().copy(isDeleted = false) },
        findChecklistUseCase = useCaseList.findUseCase { Arb.checklist().next().copy(isDeleted = false) },
        findQrUseCase = useCaseList.findUseCase { Arb.qr().next().copy(isDeleted = false) },
        findMusicUseCase = useCaseList.findUseCase { Arb.music().next().copy(isDeleted = false) },
        findDDayUseCase = useCaseList.findUseCase { Arb.dday().next().copy(isDeleted = false) },
        findTagUseCase = useCaseList.findUseCase { Arb.tag().next().copy(isDeleted = false) },
        addEntityTagUseCase = useCaseList.useCase(),
        removeEntityTagUseCase = useCaseList.useCase(),
    )
}
